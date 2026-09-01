package dev.jpitarch.ctrlgym.core.domain;

import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberGuardianAuthorization {

  private UUID id;

  private UUID memberId;

  private String guardianFirstName;

  private String guardianLastName;

  private String guardianDni;

  private String guardianEmail;

  private GuardianConsentStatus status;

  private String token;

  private OffsetDateTime tokenExpiresAt;

  private OffsetDateTime requestedAt;

  private OffsetDateTime approvedAt;

  private String approvedIp;

  private String approvedUserAgent;

  private boolean requiresAccompaniment;

  private Member member;
}
