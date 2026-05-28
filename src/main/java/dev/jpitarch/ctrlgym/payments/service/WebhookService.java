package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  public void processWebhook(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid webhook signature", e);
    }

    switch (event.getType()) {
      case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
      case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
      case "payment_intent.canceled" -> handlePaymentIntentCanceled(event);
      case "account.updated" -> handleAccountUpdated(event);
      default -> log.info("Unhandled event type: {}", event.getType());
    }
  }

  private void handlePaymentIntentSucceeded(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
      .getObject()
      .orElseThrow(() -> new IllegalStateException("Unable to deserialize payment intent"));

    String membershipId = paymentIntent.getMetadata().get("membership_id");
    String customerEmail = paymentIntent.getMetadata().get("customer_email");

    log.info("Payment succeeded for membership: {} by customer: {}", membershipId, customerEmail);
  }

  private void handlePaymentIntentFailed(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
      .getObject()
      .orElseThrow(() -> new IllegalStateException("Unable to deserialize payment intent"));

    String membershipId = paymentIntent.getMetadata().get("membership_id");

    log.error("Payment failed for membership: {}", membershipId);
  }

  private void handlePaymentIntentCanceled(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
      .getObject()
      .orElseThrow(() -> new IllegalStateException("Unable to deserialize payment intent"));

    String membershipId = paymentIntent.getMetadata().get("membership_id");

    log.warn("Payment canceled for membership: {}", membershipId);
  }

  private void handleAccountUpdated(Event event) {
    event.getDataObjectDeserializer()
      .getObject()
      .ifPresent(stripeObject -> {
        Account account = (Account) stripeObject;
        String accountId = account.getId();

        boolean onboardingComplete =
          Boolean.TRUE.equals(account.getChargesEnabled()) &&
            Boolean.TRUE.equals(account.getPayoutsEnabled());

        if (onboardingComplete) {
          log.info("Onboarding completed for account: {}", accountId);
        }
      });
  }
}
