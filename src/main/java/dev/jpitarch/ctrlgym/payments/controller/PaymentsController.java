package dev.jpitarch.ctrlgym.payments.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.payments.dto.*;
import dev.jpitarch.ctrlgym.payments.service.MembershipService;
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

  @PostMapping("/members/{memberId}/payment-methods")
  public ResponseEntity<SetupIntentResponse> createIntent(@PathVariable UUID memberId) throws StripeException {
    SetupIntentResponse response = membershipService.createSetupIntent(memberId);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/members/{memberId}/memberships/{membershipId}")
  public ResponseEntity<Void> initializeMembership(@PathVariable UUID memberId, @PathVariable String membershipId) throws StripeException {
    membershipService.initializeMembership(memberId, membershipId);
    return ResponseEntity.noContent().build();
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
