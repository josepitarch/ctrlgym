package dev.jpitarch.ctrlgym.core.services;

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
import dev.jpitarch.ctrlgym.core.entities.GymEntity;
import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import dev.jpitarch.ctrlgym.core.events.GuardianAuthorizationRequiredEvent;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.LegalDocumentsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MemberGuardianAuthorizationRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardianAuthorizationServiceTest {

  @Mock
  MemberGuardianAuthorizationRepository repository;

  @Mock
  MembersRepository memberRepository;

  @Mock
  EmailTemplateComponent emailTemplateComponent;

  @Mock
  EmailService emailService;

  @Mock
  GymsRepository gymsRepository;

  @Mock
  LegalDocumentsRepository legalDocumentsRepository;

  @InjectMocks
  GuardianAuthorizationService service;

  private final UUID memberId = UUID.randomUUID();
  private final UUID termsVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000001");
  private final UUID privacyVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000002");
  private final Integer gymId = 1;

  private Member buildMember() {
    return Member.builder()
      .id(memberId)
      .name("John")
      .firstSurname("Doe")
      .secondSurname("Smith")
      .birthDate(LocalDate.of(2008, 5, 15))
      .build();
  }

  private MemberGuardianAuthorization buildAuth(String token, GuardianConsentStatus status, OffsetDateTime expiresAt) {
    Member member = buildMember();
    return MemberGuardianAuthorization.builder()
      .id(UUID.randomUUID())
      .memberId(memberId)
      .member(member)
      .status(status)
      .token(token)
      .tokenExpiresAt(expiresAt)
      .requestedAt(OffsetDateTime.now().minusDays(1))
      .requiresAccompaniment(false)
      .build();
  }

  private GuardianApprovalRequest buildApprovalRequest(List<UUID> docIds) {
    return new GuardianApprovalRequest("Jane", "Doe", "Smith", docIds);
  }

  @Nested
  @DisplayName("getByToken")
  class GetByToken {

    @Test
    @DisplayName("Valid token returns DTO with terms of use")
    void validToken_returnsDtoWithTermsOfUse() {
      String token = "valid-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      when(memberRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);

      LegalDocumentVersion termsDoc = LegalDocumentVersion.builder()
        .id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).version("1.0")
        .content("Terms content").effectiveDate(LocalDate.of(2025, 1, 1)).active(true).build();
      when(legalDocumentsRepository.findActiveByGymIdAndType(gymId, LegalDocumentType.TERMS_OF_USE))
        .thenReturn(Optional.of(termsDoc));

      GuardianAuthorizationDto dto = service.getByToken(token);

      assertThat(dto.memberFirstName()).isEqualTo("John");
      assertThat(dto.memberLastName()).isEqualTo("Doe");
      assertThat(dto.memberDateOfBirth()).isEqualTo(LocalDate.of(2008, 5, 15));
      assertThat(dto.gymName()).isEqualTo("Wolf Gym");
      assertThat(dto.requiresAccompaniment()).isFalse();
      assertThat(dto.status()).isEqualTo(GuardianConsentStatus.PENDING);
      assertThat(dto.termsOfUse()).isNotNull();
      assertThat(dto.termsOfUse().documentVersionId()).isEqualTo(termsVersionId);
      assertThat(dto.termsOfUse().type()).isEqualTo(LegalDocumentType.TERMS_OF_USE);
      assertThat(dto.termsOfUse().content()).isEqualTo("Terms content");
    }

    @Test
    @DisplayName("Valid token returns DTO with null terms of use when no active document exists")
    void validToken_returnsDtoWithNullTermsOfUse() {
      String token = "valid-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      when(memberRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
      when(legalDocumentsRepository.findActiveByGymIdAndType(gymId, LegalDocumentType.TERMS_OF_USE))
        .thenReturn(Optional.empty());

      GuardianAuthorizationDto dto = service.getByToken(token);

      assertThat(dto.termsOfUse()).isNull();
    }

    @Test
    @DisplayName("Expired PENDING token transitions to EXPIRED status")
    void expiredPendingToken_transitionsToExpired() {
      String token = "expired-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().minusDays(1));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      when(memberRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
      when(legalDocumentsRepository.findActiveByGymIdAndType(gymId, LegalDocumentType.TERMS_OF_USE))
        .thenReturn(Optional.empty());

      GuardianAuthorizationDto dto = service.getByToken(token);

      assertThat(dto.status()).isEqualTo(GuardianConsentStatus.EXPIRED);
      verify(repository).save(auth);
    }

    @Test
    @DisplayName("Expired non-PENDING token does not change status")
    void expiredNonPendingToken_doesNotChangeStatus() {
      String token = "expired-approved-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.APPROVED, OffsetDateTime.now().minusDays(1));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      when(memberRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
      when(legalDocumentsRepository.findActiveByGymIdAndType(gymId, LegalDocumentType.TERMS_OF_USE))
        .thenReturn(Optional.empty());

      GuardianAuthorizationDto dto = service.getByToken(token);

      assertThat(dto.status()).isEqualTo(GuardianConsentStatus.APPROVED);
      verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Token not found throws AuthorizationNotFoundException")
    void tokenNotFound_throwsException() {
      when(repository.findByToken("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getByToken("nonexistent"))
        .isInstanceOf(AuthorizationNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("approve")
  class Approve {

    @Test
    @DisplayName("Valid approval sets guardian data, status, and saves acceptances")
    void validApproval_setsDataAndSavesAcceptances() {
      String token = "valid-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));

      List<LegalDocumentVersion> activeVersions = List.of(
        LegalDocumentVersion.builder().id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).active(true).build(),
        LegalDocumentVersion.builder().id(privacyVersionId).type(LegalDocumentType.PRIVACY_POLICY).active(true).build()
      );
      when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId))).thenReturn(activeVersions);

      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId, privacyVersionId));

      service.approve(token, request, "192.168.1.1", "TestAgent/1.0");

      assertThat(auth.getGuardianFirstName()).isEqualTo("Jane");
      assertThat(auth.getGuardianLastName()).isEqualTo("Doe");
      assertThat(auth.getStatus()).isEqualTo(GuardianConsentStatus.APPROVED);
      assertThat(auth.getApprovedAt()).isNotNull();
      assertThat(auth.getApprovedIp()).isEqualTo("192.168.1.1");
      assertThat(auth.getApprovedUserAgent()).isEqualTo("TestAgent/1.0");

      verify(repository).save(auth);
      verify(memberRepository).updateStatus(memberId, UserStatus.ACTIVE);

      ArgumentCaptor<MemberTermsAcceptanceEntity> acceptanceCaptor = ArgumentCaptor.forClass(MemberTermsAcceptanceEntity.class);
      verify(legalDocumentsRepository, times(2)).saveAcceptance(acceptanceCaptor.capture());

      var acceptances = acceptanceCaptor.getAllValues();
      assertThat(acceptances).hasSize(2);
      for (var acceptance : acceptances) {
        assertThat(acceptance.getMemberId()).isEqualTo(memberId);
        assertThat(acceptance.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(acceptance.getUserAgent()).isEqualTo("TestAgent/1.0");
        assertThat(acceptance.getAcceptedAt()).isNotNull();
      }
      assertThat(acceptances.get(0).getDocumentVersionId()).isEqualTo(termsVersionId);
      assertThat(acceptances.get(1).getDocumentVersionId()).isEqualTo(privacyVersionId);
    }

    @Test
    @DisplayName("Token not found throws AuthorizationNotFoundException")
    void tokenNotFound_throwsException() {
      when(repository.findByToken("nonexistent")).thenReturn(Optional.empty());
      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId));

      assertThatThrownBy(() -> service.approve("nonexistent", request, "127.0.0.1", "Mozilla"))
        .isInstanceOf(AuthorizationNotFoundException.class);
    }

    @Test
    @DisplayName("Already APPROVED token throws InvalidAuthorizationStateException")
    void alreadyApproved_throwsException() {
      String token = "approved-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.APPROVED, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId));

      assertThatThrownBy(() -> service.approve(token, request, "127.0.0.1", "Mozilla"))
        .isInstanceOf(InvalidAuthorizationStateException.class);

      verify(repository, never()).save(any());
      verify(memberRepository, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("EXPIRED status token throws InvalidAuthorizationStateException")
    void expiredStatus_throwsException() {
      String token = "expired-status-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.EXPIRED, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId));

      assertThatThrownBy(() -> service.approve(token, request, "127.0.0.1", "Mozilla"))
        .isInstanceOf(InvalidAuthorizationStateException.class);

      verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expired token throws AuthorizationTokenExpiredException")
    void expiredToken_throwsException() {
      String token = "expired-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().minusDays(1));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId));

      assertThatThrownBy(() -> service.approve(token, request, "127.0.0.1", "Mozilla"))
        .isInstanceOf(AuthorizationTokenExpiredException.class);

      verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Stale (inactive) document throws StaleLegalDocumentException")
    void staleDocument_throwsException() {
      String token = "valid-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));

      List<LegalDocumentVersion> versions = List.of(
        LegalDocumentVersion.builder().id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).active(false).build()
      );
      when(legalDocumentsRepository.findAllById(List.of(termsVersionId))).thenReturn(versions);
      GuardianApprovalRequest request = buildApprovalRequest(List.of(termsVersionId));

      assertThatThrownBy(() -> service.approve(token, request, "127.0.0.1", "Mozilla"))
        .isInstanceOf(StaleLegalDocumentException.class);

      verify(repository, never()).save(any());
      verify(memberRepository, never()).updateStatus(any(), any());
      verify(legalDocumentsRepository, never()).saveAcceptance(any());
    }

    @Test
    @DisplayName("Approval with empty document list proceeds without saving acceptances")
    void emptyDocumentList_proceedsWithoutAcceptances() {
      String token = "valid-token";
      MemberGuardianAuthorization auth = buildAuth(token, GuardianConsentStatus.PENDING, OffsetDateTime.now().plusDays(5));
      when(repository.findByToken(token)).thenReturn(Optional.of(auth));
      when(legalDocumentsRepository.findAllById(List.of())).thenReturn(List.of());

      GuardianApprovalRequest request = buildApprovalRequest(List.of());

      service.approve(token, request, "10.0.0.1", "Agent");

      assertThat(auth.getStatus()).isEqualTo(GuardianConsentStatus.APPROVED);
      verify(repository).save(auth);
      verify(memberRepository).updateStatus(memberId, UserStatus.ACTIVE);
      verify(legalDocumentsRepository, never()).saveAcceptance(any());
    }
  }

  @Nested
  @DisplayName("handleGuardianAuthorizationRequiredEvent")
  class HandleEvent {

    @Test
    @DisplayName("Event creates authorization and sends email")
    void event_createsAuthorizationAndSendsEmail() {
      UUID eventId = UUID.randomUUID();
      GuardianAuthorizationRequiredEvent event = new GuardianAuthorizationRequiredEvent(
        this, eventId, gymId, "guardian@test.com"
      );

      Member member = buildMember();
      ReflectionTestUtils.setField(member, "id", eventId);
      when(memberRepository.getById(eventId)).thenReturn(member);
      when(memberRepository.getGymIdByMemberId(eventId)).thenReturn(gymId);

      GymEntity gym = new GymEntity();
      gym.setId(gymId);
      gym.setName("Wolf Gym");
      when(gymsRepository.getById(gymId)).thenReturn(gym);
      when(emailTemplateComponent.build(eq("guardian-authorization.html"), any())).thenReturn("<html>email</html>");

      service.handleGuardianAuthorizationRequiredEvent(event);

      ArgumentCaptor<MemberGuardianAuthorization> authCaptor = ArgumentCaptor.forClass(MemberGuardianAuthorization.class);
      verify(repository).save(authCaptor.capture());

      var savedAuth = authCaptor.getValue();
      assertThat(savedAuth.getMemberId()).isEqualTo(eventId);
      assertThat(savedAuth.getStatus()).isEqualTo(GuardianConsentStatus.PENDING);
      assertThat(savedAuth.getToken()).isNotBlank();
      assertThat(savedAuth.getTokenExpiresAt()).isAfter(OffsetDateTime.now());
      assertThat(savedAuth.isRequiresAccompaniment()).isFalse();

      verify(emailService).send(
        eq("guardian@test.com"),
        eq("[Wolf Gym] Autorización requerida para inscripción"),
        eq("<html>email</html>")
      );
    }
  }
}
