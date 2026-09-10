package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidNifException;
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
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import dev.jpitarch.ctrlgym.verifactu.services.NifValidationService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  JwtFactory jwtFactory;

  @Mock
  RefreshTokenService refreshTokenService;

  @Mock
  LegalDocumentsRepository legalDocumentsRepository;

  @Mock
  ApplicationEventPublisher eventPublisher;

  @Mock
  NifValidationService nifValidationService;

  @Mock
  CustomerService customerService;

  @InjectMocks
  SignupService signupService;

  private final UUID termsVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000001");
  private final UUID privacyVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000002");
  private final Integer gymId = 1;

  private SignupRequest adultRequest(List<UUID> acceptedDocIds) {
    return new SignupRequest(
      "adult@test.com",
      "Password1!",
      "Adult",
      "User",
      null,
      null,
      "MALE",
      LocalDate.of(1995, 8, 20),
      acceptedDocIds
    );
  }

  private SignupRequest adultRequestWithNif(List<UUID> acceptedDocIds, String nif) {
    return new SignupRequest(
      "adult@test.com",
      "Password1!",
      "Adult",
      "User",
      null,
      nif,
      "MALE",
      LocalDate.of(1995, 8, 20),
      acceptedDocIds
    );
  }

  private SignupRequest minorRequest(List<UUID> acceptedDocIds) {
    return new SignupRequest(
      "minor@test.com",
      "Password1!",
      "Minor",
      "User",
      null,
      null,
      "MALE",
      LocalDate.now().minusYears(16),
      acceptedDocIds
    );
  }

  private List<LegalDocumentVersion> activeMandatoryVersions() {
    return List.of(
      LegalDocumentVersion.builder().id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).active(true).build(),
      LegalDocumentVersion.builder().id(privacyVersionId).type(LegalDocumentType.PRIVACY_POLICY).active(true).build()
    );
  }

  @Test
  @DisplayName("Signup adult with all mandatory documents returns AuthResponse and sets ACTIVE status")
  void signup_adultWithAllMandatoryDocuments_returnsAuthResponseAndSetsActive() throws StripeException {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId)))
      .thenReturn(activeMandatoryVersions());
    when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
    when(jwtFactory.generateAccessToken(any(UserEntity.class))).thenReturn("access-token");
    when(refreshTokenService.generateRawRefreshToken(any(UUID.class), eq(gymId))).thenReturn("refresh-token");

    var request = adultRequest(List.of(termsVersionId, privacyVersionId));
    AuthResponse response = signupService.signup(request, gymId, "127.0.0.1", "Mozilla");

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.expiresIn()).isEqualTo(900);
    assertThat(response.tokenType()).isEqualTo("Bearer");

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);

    verify(eventPublisher, never()).publishEvent(any(GuardianAuthorizationRequiredEvent.class));
    verify(legalDocumentsRepository, times(2)).saveAcceptance(any(MemberTermsAcceptanceEntity.class));
  }

  @Test
  @DisplayName("Signup minor sets PENDING_GUARDIAN_CONSENT and publishes GuardianAuthorizationRequiredEvent")
  void signup_minor_setsPendingGuardianConsentAndPublishesEvent() throws StripeException {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId)))
      .thenReturn(activeMandatoryVersions());
    when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
    when(jwtFactory.generateAccessToken(any(UserEntity.class))).thenReturn("access-token");
    when(refreshTokenService.generateRawRefreshToken(any(UUID.class), eq(gymId))).thenReturn("refresh-token");

    var request = minorRequest(List.of(termsVersionId, privacyVersionId));
    AuthResponse response = signupService.signup(request, gymId, "127.0.0.1", "Mozilla");

    assertThat(response).isNotNull();

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING_GUARDIAN_CONSENT);

    ArgumentCaptor<GuardianAuthorizationRequiredEvent> eventCaptor = ArgumentCaptor.forClass(GuardianAuthorizationRequiredEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getMemberId()).isEqualTo(userCaptor.getValue().getId());
    assertThat(eventCaptor.getValue().getGymId()).isEqualTo(gymId);
  }

  @Test
  @DisplayName("Signup with null birthDate is treated as minor")
  void signup_nullBirthDate_treatedAsMinor() throws StripeException {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId)))
      .thenReturn(activeMandatoryVersions());
    when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
    when(jwtFactory.generateAccessToken(any(UserEntity.class))).thenReturn("access-token");
    when(refreshTokenService.generateRawRefreshToken(any(UUID.class), eq(gymId))).thenReturn("refresh-token");

    var request = new SignupRequest(
      "nullbirth@test.com",
      "Password1!",
      "NoBirth",
      "User",
      null,
      null,
      null,
      null,
      List.of(termsVersionId, privacyVersionId)
    );
    signupService.signup(request, gymId, "127.0.0.1", "Mozilla");

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING_GUARDIAN_CONSENT);
    verify(eventPublisher).publishEvent(any(GuardianAuthorizationRequiredEvent.class));
  }

  @Test
  @DisplayName("Signup missing TERMS_OF_USE throws MissingMandatoryAcceptanceException")
  void signup_missingTermsOfUse_throwsException() {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    var privacyOnly = List.of(
      LegalDocumentVersion.builder().id(privacyVersionId).type(LegalDocumentType.PRIVACY_POLICY).active(true).build()
    );
    when(legalDocumentsRepository.findAllById(List.of(privacyVersionId))).thenReturn(privacyOnly);

    var request = adultRequest(List.of(privacyVersionId));

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(MissingMandatoryAcceptanceException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup missing PRIVACY_POLICY throws MissingMandatoryAcceptanceException")
  void signup_missingPrivacyPolicy_throwsException() {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    var termsOnly = List.of(
      LegalDocumentVersion.builder().id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).active(true).build()
    );
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId))).thenReturn(termsOnly);

    var request = adultRequest(List.of(termsVersionId));

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(MissingMandatoryAcceptanceException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup with stale (inactive) document throws StaleLegalDocumentException")
  void signup_staleDocument_throwsException() {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    var staleVersions = List.of(
      LegalDocumentVersion.builder().id(termsVersionId).type(LegalDocumentType.TERMS_OF_USE).active(false).build(),
      LegalDocumentVersion.builder().id(privacyVersionId).type(LegalDocumentType.PRIVACY_POLICY).active(true).build()
    );
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId))).thenReturn(staleVersions);

    var request = adultRequest(List.of(termsVersionId, privacyVersionId));

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(StaleLegalDocumentException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup with null acceptedDocumentVersionIds throws MissingMandatoryAcceptanceException")
  void signup_nullAcceptedDocumentVersionIds_throwsException() {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of())).thenReturn(List.of());

    var request = adultRequest(null);

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(MissingMandatoryAcceptanceException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup with empty acceptedDocumentVersionIds throws MissingMandatoryAcceptanceException")
  void signup_emptyAcceptedDocumentVersionIds_throwsException() {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of())).thenReturn(List.of());

    var request = adultRequest(List.of());

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(MissingMandatoryAcceptanceException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup saves acceptances with correct ip and userAgent")
  void signup_savesAcceptancesWithCorrectIpAndUserAgent() throws StripeException {
    when(nifValidationService.validateNif(any(), any(), anyString())).thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId)))
      .thenReturn(activeMandatoryVersions());
    when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
    when(jwtFactory.generateAccessToken(any(UserEntity.class))).thenReturn("access-token");
    when(refreshTokenService.generateRawRefreshToken(any(UUID.class), eq(gymId))).thenReturn("refresh-token");

    var request = adultRequest(List.of(termsVersionId, privacyVersionId));
    signupService.signup(request, gymId, "192.168.1.1", "TestAgent/1.0");

    ArgumentCaptor<MemberTermsAcceptanceEntity> acceptanceCaptor = ArgumentCaptor.forClass(MemberTermsAcceptanceEntity.class);
    verify(legalDocumentsRepository, times(2)).saveAcceptance(acceptanceCaptor.capture());

    for (var acceptance : acceptanceCaptor.getAllValues()) {
      assertThat(acceptance.getIpAddress()).isEqualTo("192.168.1.1");
      assertThat(acceptance.getUserAgent()).isEqualTo("TestAgent/1.0");
      assertThat(acceptance.getAcceptedAt()).isNotNull();
    }
  }

  @Test
  @DisplayName("Signup with invalid NIF throws InvalidNifException")
  void signup_invalidNif_throwsException() {
    when(nifValidationService.validateNif(eq(gymId), eq("B99999999"), anyString()))
      .thenReturn(false);

    var request = adultRequestWithNif(List.of(termsVersionId, privacyVersionId), "B99999999");

    assertThatThrownBy(() -> signupService.signup(request, gymId, "127.0.0.1", "Mozilla"))
      .isInstanceOf(InvalidNifException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup with valid NIF proceeds normally")
  void signup_validNif_proceedsNormally() throws StripeException {
    when(nifValidationService.validateNif(eq(gymId), eq("B86561412"), anyString()))
      .thenReturn(true);
    when(legalDocumentsRepository.findAllById(List.of(termsVersionId, privacyVersionId)))
      .thenReturn(activeMandatoryVersions());
    when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
    when(jwtFactory.generateAccessToken(any(UserEntity.class))).thenReturn("access-token");
    when(refreshTokenService.generateRawRefreshToken(any(UUID.class), eq(gymId))).thenReturn("refresh-token");

    var request = adultRequestWithNif(List.of(termsVersionId, privacyVersionId), "B86561412");
    AuthResponse response = signupService.signup(request, gymId, "127.0.0.1", "Mozilla");

    assertThat(response.accessToken()).isEqualTo("access-token");
    verify(nifValidationService).validateNif(eq(gymId), eq("B86561412"), anyString());
    verify(userRepository).save(any(UserEntity.class));
  }
}
