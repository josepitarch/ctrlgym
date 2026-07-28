package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.AuthResponse;
import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
  UsersRepository usersRepository;

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
    var result = authService.signin(request);

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
  @DisplayName("signup calls Supabase signup endpoint when user does not exist")
  void signup_callsSupabaseAuth_whenUserDoesNotExist() {
    when(usersRepository.findIdByEmail("new@example.com")).thenReturn(Optional.empty());
    when(responseSpec.body(AuthResponse.class)).thenReturn(authResponse);

    var request = new SignupRequest("new@example.com", "password123", 1, "John", "Doe", "Smith");
    var result = authService.signup(request);

    assertThat(result).isEqualTo(authResponse);
    verify(requestBodyUriSpec).uri("/signup");
    verify(usersRepository, never()).save(any());

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
  @DisplayName("signup inserts user row and signs in when user already exists")
  void signup_insertsUserRowAndSignsIn_whenUserAlreadyExists() {
    UUID existingUserId = UUID.randomUUID();
    when(usersRepository.findIdByEmail("existing@example.com")).thenReturn(Optional.of(existingUserId));
    when(responseSpec.body(AuthResponse.class)).thenReturn(authResponse);

    var request = new SignupRequest("existing@example.com", "password123", 1, "Jane", "Doe", "Smith");
    var result = authService.signup(request);

    assertThat(result).isEqualTo(authResponse);

    ArgumentCaptor<UserMO> userCaptor = ArgumentCaptor.forClass(UserMO.class);
    verify(usersRepository).save(userCaptor.capture());
    UserMO savedUser = userCaptor.getValue();
    assertThat(savedUser.getId()).isEqualTo(existingUserId);
    assertThat(savedUser.getGymId()).isEqualTo(1);
    assertThat(savedUser.getEmail()).isEqualTo("existing@example.com");
    assertThat(savedUser.getName()).isEqualTo("Jane");
    assertThat(savedUser.getFirstSurname()).isEqualTo("Doe");
    assertThat(savedUser.getSecondSurname()).isEqualTo("Smith");

    verify(requestBodyUriSpec).uri("/token?grant_type=password");
    verify(requestBodyUriSpec, never()).uri("/signup");
  }
}
