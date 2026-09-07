package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.domain.LegalDocumentVersion;
import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MissingMandatoryAcceptanceException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.StaleLegalDocumentException;
import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.events.GuardianAuthorizationRequiredEvent;
import dev.jpitarch.ctrlgym.core.repositories.LegalDocumentsRepository;
import dev.jpitarch.ctrlgym.lib.AgeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignupService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final RefreshTokenService refreshTokenService;

  private final LegalDocumentsRepository legalDocumentsRepository;

  private final ApplicationEventPublisher eventPublisher;

  private static final Set<LegalDocumentType> MANDATORY_TYPES =
    Set.of(LegalDocumentType.TERMS_OF_USE, LegalDocumentType.PRIVACY_POLICY);

  @Transactional
  public AuthResponse signup(SignupRequest request, Integer gymId, String ip, String userAgent) {
    List<LegalDocumentVersion> acceptedVersions = legalDocumentsRepository.findAllById(
      request.acceptedDocumentVersionIds() != null ? request.acceptedDocumentVersionIds() : List.of()
    );

    for (LegalDocumentVersion version : acceptedVersions) {
      if (!version.isActive()) {
        throw new StaleLegalDocumentException(version.getType());
      }
    }

    Set<LegalDocumentType> acceptedTypes = acceptedVersions.stream()
      .map(LegalDocumentVersion::getType)
      .collect(Collectors.toSet());

    for (LegalDocumentType mandatory : MANDATORY_TYPES) {
      if (!acceptedTypes.contains(mandatory)) {
        throw new MissingMandatoryAcceptanceException(mandatory);
      }
    }

    String hashedPassword = passwordEncoder.encode(request.password());
    UserEntity created = userRepository.create(
      request.email(),
      hashedPassword,
      gymId,
      request.name(),
      request.firstSurname(),
      request.secondSurname(),
      request.gender(),
      request.birthDate()
    );

    if (AgeHelper.isAdult(request.birthDate())) {
      created.setStatus(UserStatus.ACTIVE);
    } else {
      created.setStatus(UserStatus.PENDING_GUARDIAN_CONSENT);
      eventPublisher.publishEvent(new GuardianAuthorizationRequiredEvent(this, created.getId(), gymId));
    }
    userRepository.save(created);

    for (LegalDocumentVersion version : acceptedVersions) {
      var acceptance = new MemberTermsAcceptanceEntity();
      acceptance.setId(UUID.randomUUID());
      acceptance.setMemberId(created.getId());
      acceptance.setDocumentVersionId(version.getId());
      acceptance.setAcceptedAt(OffsetDateTime.now());
      acceptance.setIpAddress(ip);
      acceptance.setUserAgent(userAgent);
      legalDocumentsRepository.saveAcceptance(acceptance);
    }

    String accessToken = jwtService.generateAccessToken(created);
    String rawRefreshToken = refreshTokenService.generateRawRefreshToken(created.getId(), gymId);

    return new AuthResponse(accessToken, rawRefreshToken, 900, "Bearer");
  }
}
