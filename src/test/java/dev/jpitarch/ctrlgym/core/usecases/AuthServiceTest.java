package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthException;
import dev.jpitarch.ctrlgym.core.dto.AuthResponse;
import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  AuthService authService;

  @Mock
  RestClient supabaseAuthRestClient;

  @Mock
  MembersRepository membersRepository;

  @Mock
  RestClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  RestClient.RequestBodySpec requestBodySpec;

  @Mock
  RestClient.ResponseSpec responseSpec;

  private final AuthResponse authResponse = new AuthResponse("access-token", "refresh-token", 3600, "Bearer");

  @BeforeEach
  void setUp() {
    lenient().when(supabaseAuthRestClient.post()).thenReturn(requestBodyUriSpec);
    lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  @DisplayName("signin returns AuthResponse from Supabase")
  void signin_returnsAuthResponse_whenValidRequest() {
    when(responseSpec.body(AuthResponse.class)).thenReturn(authResponse);

    var request = new SigninRequest("test@example.com", "password123");
    var result = authService.login(request);

    assertThat(result).isEqualTo(authResponse);
    verify(requestBodyUriSpec).uri("/token?grant_type=password");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
    verify(requestBodySpec).body(bodyCaptor.capture());
    assertThat(bodyCaptor.getValue())
      .containsEntry("email", "test@example.com")
      .containsEntry("password", "password123");
  }

  @Test
  @DisplayName("signup calls Supabase signup endpoint when user does not exist in any gym")
  void signup_callsSupabaseAuth_whenUserDoesNotExist() {
    when(membersRepository.exists(1, "new@example.com")).thenReturn(false);
    when(membersRepository.existsAnotherGym(1, "new@example.com")).thenReturn(false);
    when(responseSpec.body(AuthResponse.class)).thenReturn(authResponse);

    var request = new SignupRequest("new@example.com", "password123", 1, "John", "Doe", "Smith");
    var result = authService.signup(request);

    assertThat(result).isEqualTo(authResponse);
    verify(requestBodyUriSpec).uri("/signup");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
    verify(requestBodySpec).body(bodyCaptor.capture());
    Map<String, Object> body = bodyCaptor.getValue();
    assertThat(body)
      .containsEntry("email", "new@example.com")
      .containsEntry("password", "password123")
      .containsKey("data");
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertThat(data)
      .containsEntry("gym_id", 1)
      .containsEntry("name", "John")
      .containsEntry("first_surname", "Doe")
      .containsEntry("second_surname", "Smith");
  }

  @Test
  @DisplayName("signup throws AuthException ALREADY_EXISTS when user exists and is not in migration")
  void signup_throwsAlreadyExists_whenUserExistsInSameGym() {
    when(membersRepository.exists(1, "existing@example.com")).thenReturn(true);
    when(membersRepository.isInMigration(1, "existing@example.com")).thenReturn(false);

    var request = new SignupRequest("existing@example.com", "password123", 1, "Jane", "Doe", "Smith");

    assertThatThrownBy(() -> authService.signup(request))
      .isInstanceOf(AuthException.class)
      .hasMessageContaining("ALREADY_EXISTS");

    verify(requestBodyUriSpec, never()).uri("/signup");
    verify(requestBodyUriSpec, never()).uri("/invite");
  }

  @Test
  @DisplayName("signup resends invitation and throws AuthException IS_IN_MIGRATION when user is in migration")
  void signup_resendsInvitation_whenUserInMigration() {
    when(membersRepository.exists(1, "migration@example.com")).thenReturn(true);
    when(membersRepository.isInMigration(1, "migration@example.com")).thenReturn(true);
    when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

    var request = new SignupRequest("migration@example.com", "password123", 1, "Jane", "Doe", "Smith");

    assertThatThrownBy(() -> authService.signup(request))
      .isInstanceOf(AuthException.class)
      .hasMessageContaining("IS_IN_MIGRATION");

    verify(requestBodyUriSpec).uri("/invite");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
    verify(requestBodySpec).body(bodyCaptor.capture());
    assertThat(bodyCaptor.getValue())
      .containsEntry("email", "migration@example.com");

    verify(requestBodyUriSpec, never()).uri("/signup");
  }

  @Test
  @DisplayName("signup throws AuthException ANOTHER_GYM when user exists in another gym")
  void signup_throwsAnotherGym_whenUserExistsInAnotherGym() {
    when(membersRepository.exists(2, "another@example.com")).thenReturn(false);
    when(membersRepository.existsAnotherGym(2, "another@example.com")).thenReturn(true);

    var request = new SignupRequest("another@example.com", "password123", 2, "Bob", "Doe", "Smith");

    assertThatThrownBy(() -> authService.signup(request))
      .isInstanceOf(AuthException.class)
      .hasMessageContaining("ANOTHER_GYM");

    verify(requestBodyUriSpec, never()).uri("/signup");
  }
}
