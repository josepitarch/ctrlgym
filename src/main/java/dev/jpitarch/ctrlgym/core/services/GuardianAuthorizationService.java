package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberGuardianAuthorization;
import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthorizationNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.InvalidAuthorizationStateException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthorizationTokenExpiredException;
import dev.jpitarch.ctrlgym.core.dto.GuardianAuthorizationDto;
import dev.jpitarch.ctrlgym.core.repositories.MemberGuardianAuthorizationRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus.PENDING;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianAuthorizationService {

  private final MemberGuardianAuthorizationRepository repository;

  private final MembersRepository memberRepository;

  public GuardianAuthorizationDto getByToken(String token) {
    MemberGuardianAuthorization auth = repository.findByToken(token)
      .orElseThrow(() -> new AuthorizationNotFoundException(token));

    if (auth.getTokenExpiresAt().isBefore(OffsetDateTime.now()) && auth.getStatus() == PENDING) {
      auth.setStatus(GuardianConsentStatus.EXPIRED);
      repository.save(auth);
    }

    return toDto(auth);
  }

  @Transactional
  public void approve(String token, String ipAddress, String userAgent) {
    MemberGuardianAuthorization auth = repository.findByToken(token)
      .orElseThrow(() -> new AuthorizationNotFoundException(token));

    if (auth.getStatus() != GuardianConsentStatus.PENDING) {
      throw new InvalidAuthorizationStateException(auth.getStatus());
    }

    if (auth.getTokenExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new AuthorizationTokenExpiredException(token);
    }

    auth.setStatus(GuardianConsentStatus.APPROVED);
    auth.setApprovedAt(OffsetDateTime.now());
    auth.setApprovedIp(ipAddress);
    auth.setApprovedUserAgent(userAgent);
    repository.save(auth);

    Member member = auth.getMember();
    member.setStatus(UserStatus.ACTIVE);
    memberRepository.save(member, null);
  }

  private GuardianAuthorizationDto toDto(MemberGuardianAuthorization auth) {
    Member member = auth.getMember();
    return new GuardianAuthorizationDto(
      member.getName(),
      member.getFirstSurname(),
      member.getBirthDate(),
      "Wolf Gym",
      buildAuthorizationText(member),
      auth.isRequiresAccompaniment(),
      auth.getStatus()
    );
  }

  private String buildAuthorizationText(Member member) {
    return "Como padre/madre/tutor legal de " + member.getFullName()
      + ", autorizo su inscripción como socio del centro y la aceptación en su nombre "
      + "de las condiciones generales, política de privacidad y demás documentos aplicables.";
  }
}
