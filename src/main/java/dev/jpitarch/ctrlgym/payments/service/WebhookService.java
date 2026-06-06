package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.SetupIntent;
import com.stripe.net.Webhook;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  @Value("${stripe.whsec-account}")
  private String webhookSecret;

  private final MembersRepository membersRepository;

  public void processWebhook(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid webhook signature", e);
    }

    switch (event.getType()) {
      case "setup_intent.created" -> handleSetupIntentCreated(map(event));
      case "setup_intent.succeeded" -> handleSetupIntentSucceeded(map(event));
    }

  }

  private void handleSetupIntentCreated(SetupIntent setupIntent) {
    log.info("SetupIntent with id {} of customer {} is created", setupIntent.getId(), setupIntent.getCustomer());
  }

  private void handleSetupIntentSucceeded(SetupIntent setupIntent) {
    log.info("SetupIntent with id {} of customer {} is succeeded", setupIntent.getId(), setupIntent.getCustomer());
    membersRepository.savePaymentMethodId(setupIntent.getCustomer(), setupIntent.getPaymentMethod());
  }

  @SuppressWarnings("unchecked")
  private <T> T map(Event event) {
    return (T) event.getDataObjectDeserializer().getObject().orElseThrow();
  }

}
