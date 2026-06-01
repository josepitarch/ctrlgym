package dev.jpitarch.ctrlgym.payments.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.payments.dto.*;
import dev.jpitarch.ctrlgym.payments.service.CustomerService;
import dev.jpitarch.ctrlgym.payments.service.MembershipService;
import dev.jpitarch.ctrlgym.payments.service.PaymentService;
import dev.jpitarch.ctrlgym.payments.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

  private final PaymentService paymentService;

  private final MembershipService membershipService;

  private final WebhookService webhookService;

  @PostMapping("/products")
  public ResponseEntity<Price> createProduct(@RequestBody Map<String, String> request) throws StripeException {
    int gymId = Integer.parseInt(request.get("gymId"));
    String membershipName = request.get("membershipName");
    double unitAmount = Double.parseDouble(request.get("unitAmount"));

    var price = membershipService.createProduct(GymBranchId.of(gymId, 1000), membershipName, unitAmount);

    return ResponseEntity.ok(price);
  }

  @PostMapping("/intents")
  public ResponseEntity<SetupIntentResponse> createIntent(@RequestBody Map<String, String> request) throws StripeException {
    UUID memberId = UUID.fromString(request.get("memberId"));
    SetupIntentResponse response = membershipService.createSetupIntent(GymBranchId.of(1, 1000), memberId);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/memberships")
  public ResponseEntity<Void> createMembership(@RequestBody Map<String, String> request) throws StripeException {
    String membershipId = request.get("membershipId");
    UUID memberId = UUID.fromString(request.get("memberId"));
    String paymentMethodId = request.get("paymentMethodId");

    membershipService.initializeMembership(membershipId, memberId, GymBranchId.of(1, 1000), paymentMethodId);

    return ResponseEntity.ok().build();
  }

  @PostMapping("/accounts")
  public ResponseEntity<ConnectAccountResponse> createGymAccount(@RequestBody ConnectAccountRequest request) {
    ConnectAccountResponse response = paymentService.createGymAccount(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/accounts/{accountId}/status")
  public ResponseEntity<Boolean> isAccountActive(@PathVariable String accountId) {
    boolean isActive = paymentService.isAccountActive(accountId);
    return ResponseEntity.ok(isActive);
  }

  @PostMapping("/webhook")
  public ResponseEntity<String> handleWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature) {
    try {
      webhookService.processWebhook(payload, signature);
      return ResponseEntity.ok("Webhook processed successfully");
    } catch (Exception e) {
      log.error("Failed to process webhook", e);
      return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
    }
  }
}
