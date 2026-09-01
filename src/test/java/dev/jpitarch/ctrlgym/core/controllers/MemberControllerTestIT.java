package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.dto.CreateMemberRequest;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @MockitoBean
  CustomerService customerService;

  JsonMapper jsonMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();

  UUID memberId = UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1");
  Integer gymId = 1;

  UUID termsOfServiceVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000001");
  UUID privacyPolicyVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000002");
  UUID cookiePolicyVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000003");
  UUID staleTermsVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000004");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtAuth() {
    return jwt().jwt(j -> j.subject(memberId.toString()))
      .authorities(new SimpleGrantedAuthority("ROLE_MEMBER"));
  }

  private CreateMemberRequest buildBaseRequest() {
    var request = new CreateMemberRequest();
    request.setName("Test");
    request.setFirstSurname("Member");
    request.setSecondSurname("User");
    request.setEmail("test.member@example.com");
    request.setGender(Gender.MALE);
    request.setBirthDate(LocalDate.of(1995, 8, 20));
    request.setNif("12345678Z");

    var address = new CreateMemberRequest.Address();
    address.setCity("Valencia");
    address.setPostalCode(46001);
    request.setAddress(address);

    return request;
  }

  @Test
  @Order(1)
  @DisplayName("Creates a member successfully when all mandatory documents are accepted")
  void createMember_withAllMandatoryDocuments_returns201() throws Exception {
    var request = buildBaseRequest();
    request.setAcceptedDocumentVersionIds(List.of(termsOfServiceVersionId, privacyPolicyVersionId));

    when(customerService.create(any())).thenReturn("cus_test_member");

    mockMvc.perform(post("/v1/members/{memberId}", memberId)
        .header("X-Tenant-Id", gymId.toString())
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isCreated());
  }

  @Test
  @Order(2)
  @DisplayName("Returns 409 when not all mandatory documents are accepted")
  void createMember_missingMandatoryDocument_returns409() throws Exception {
    var request = buildBaseRequest();
    request.setAcceptedDocumentVersionIds(List.of(termsOfServiceVersionId));

    mockMvc.perform(post("/v1/members/{memberId}", memberId)
        .header("X-Tenant-Id", gymId.toString())
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isConflict());
  }

  @Test
  @Order(3)
  @DisplayName("Returns 409 when an accepted document version is not active")
  void createMember_withStaleDocument_returns409() throws Exception {
    var request = buildBaseRequest();
    request.setAcceptedDocumentVersionIds(List.of(staleTermsVersionId, privacyPolicyVersionId));

    mockMvc.perform(post("/v1/members/{memberId}", memberId)
        .header("X-Tenant-Id", gymId.toString())
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isConflict());
  }
}
