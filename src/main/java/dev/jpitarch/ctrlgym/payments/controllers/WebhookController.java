package dev.jpitarch.ctrlgym.payments.controllers;

import dev.jpitarch.ctrlgym.payments.services.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class WebhookController {

  private final WebhookService webhookService;

  @PostMapping("/webhook")
  public ResponseEntity<String> handleWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature) {
    try {
      webhookService.process(payload, signature);
      return ResponseEntity.ok("Webhook processed successfully");
    } catch (Exception e) {
      log.error("Failed to process webhook", e);
      return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
    }
  }
}
