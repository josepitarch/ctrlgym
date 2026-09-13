package dev.jpitarch.ctrlgym.core.services;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.jpitarch.ctrlgym.core.domain.LegalDocumentVersion;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberGuardianAuthorization;
import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthorizationNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthorizationTokenExpiredException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.InvalidAuthorizationStateException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.StaleLegalDocumentException;
import dev.jpitarch.ctrlgym.core.dto.GuardianApprovalRequest;
import dev.jpitarch.ctrlgym.core.dto.GuardianAuthorizationDto;
import dev.jpitarch.ctrlgym.core.dto.LegalDocumentResponse;
import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import dev.jpitarch.ctrlgym.core.events.GuardianAuthorizationRequiredEvent;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.LegalDocumentsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MemberGuardianAuthorizationRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus.PENDING;

@Slf4j
@Service
public class GuardianAuthorizationService {

  private final String baseUrl;

  private final MemberGuardianAuthorizationRepository repository;

  private final MembersRepository memberRepository;

  private final EmailTemplateComponent emailTemplateComponent;

  private final EmailService emailService;

  private final GymsRepository gymsRepository;

  private final LegalDocumentsRepository legalDocumentsRepository;

  public GuardianAuthorizationService(
    @Value("${email.redirect.base-url}") String baseUrl,
    MemberGuardianAuthorizationRepository repository,
    MembersRepository memberRepository,
    EmailTemplateComponent emailTemplateComponent,
    EmailService emailService,
    GymsRepository gymsRepository,
    LegalDocumentsRepository legalDocumentsRepository) {
    this.baseUrl = baseUrl;
    this.repository = repository;
    this.memberRepository = memberRepository;
    this.emailTemplateComponent = emailTemplateComponent;
    this.emailService = emailService;
    this.gymsRepository = gymsRepository;
    this.legalDocumentsRepository = legalDocumentsRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleGuardianAuthorizationRequiredEvent(GuardianAuthorizationRequiredEvent event) {
    Member member = memberRepository.getById(event.getMemberId());

    String token = this.generateToken();
    var auth = MemberGuardianAuthorization.builder()
      .id(UuidCreator.getTimeOrderedEpoch())
      .memberId(member.getId())
      .status(PENDING)
      .token(token)
      .tokenExpiresAt(OffsetDateTime.now().plusDays(7))
      .requestedAt(OffsetDateTime.now())
      .requiresAccompaniment(false)
      .build();

    repository.save(auth);

    Integer gymId = memberRepository.getGymIdByMemberId(event.getMemberId());
    var gym = gymsRepository.getById(gymId);

    String authorizationUrl = baseUrl + "/guardian-authorization/" + token;
    String template = emailTemplateComponent.build("guardian-authorization.html", Map.of(
      "MemberFullName", member.getFullName(),
      "AuthorizationURL", authorizationUrl,
      "GymName", gym.getName()
    ));

    emailService.send(event.getGuardianEmail(), "[%s] Autorización requerida para inscripción".formatted(gym.getName()), template);
    log.info("Guardian authorization email prepared for member {} with token {}", member.getId(), token);
  }

  public GuardianAuthorizationDto getByToken(String token) {
    MemberGuardianAuthorization auth = repository.findByToken(token)
      .orElseThrow(() -> new AuthorizationNotFoundException(token));

    if (auth.getTokenExpiresAt().isBefore(OffsetDateTime.now()) && auth.getStatus() == PENDING) {
      auth.setStatus(GuardianConsentStatus.EXPIRED);
      repository.save(auth);
    }

    Member member = auth.getMember();
    Integer gymId = memberRepository.getGymIdByMemberId(member.getId());

    LegalDocumentResponse termsOfUse = legalDocumentsRepository
      .findActiveByGymIdAndType(gymId, LegalDocumentType.TERMS_OF_USE)
      .map(doc -> new LegalDocumentResponse(
        doc.getId(),
        doc.getType(),
        doc.getVersion(),
        doc.getContent(),
        doc.getEffectiveDate()
      ))
      .orElse(null);

    return new GuardianAuthorizationDto(
      member.getName(),
      member.getFirstSurname(),
      member.getSecondSurname(),
      member.getBirthDate(),
      "Wolf Gym",
      auth.isRequiresAccompaniment(),
      auth.getStatus(),
      termsOfUse
    );
  }

  @Transactional
  public void approve(String token, GuardianApprovalRequest request, String ipAddress, String userAgent) {
    MemberGuardianAuthorization auth = repository.findByToken(token)
      .orElseThrow(() -> new AuthorizationNotFoundException(token));

    if (auth.getStatus() != GuardianConsentStatus.PENDING) {
      throw new InvalidAuthorizationStateException(auth.getStatus());
    }

    if (auth.getTokenExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new AuthorizationTokenExpiredException(token);
    }

    List<LegalDocumentVersion> acceptedVersions = legalDocumentsRepository.findAllById(
      request.acceptedDocumentVersionIds()
    );

    for (LegalDocumentVersion version : acceptedVersions) {
      if (!version.isActive()) {
        throw new StaleLegalDocumentException(version.getType());
      }
    }

    auth.setGuardianFirstName(request.name());
    auth.setGuardianLastName(request.firstSurname());
    auth.setStatus(GuardianConsentStatus.APPROVED);
    auth.setApprovedAt(OffsetDateTime.now());
    auth.setApprovedIp(ipAddress);
    auth.setApprovedUserAgent(userAgent);
    repository.save(auth);

    Member member = auth.getMember();
    member.setStatus(UserStatus.ACTIVE);
    memberRepository.updateStatus(member.getId(), UserStatus.ACTIVE);

    for (UUID docVersionId : request.acceptedDocumentVersionIds()) {
      MemberTermsAcceptanceEntity acceptance = new MemberTermsAcceptanceEntity();
      acceptance.setId(UuidCreator.getTimeOrderedEpoch());
      acceptance.setMemberId(member.getId());
      acceptance.setDocumentVersionId(docVersionId);
      acceptance.setAcceptedAt(OffsetDateTime.now());
      acceptance.setIpAddress(ipAddress);
      acceptance.setUserAgent(userAgent);
      legalDocumentsRepository.saveAcceptance(acceptance);
    }
  }

  private String generateToken() {
    byte[] randomBytes = new byte[32];
    new SecureRandom().nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }
}
