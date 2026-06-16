package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.net.Webhook;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.repositories.InvoiceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  private static final String WEBHOOK_EVENTS_DIR = "webhook-events";

  private static final Set<String> SUPPORTED_EVENTS = Set.of(
    "setup_intent.created",
    "setup_intent.succeeded",
    "invoice.created",
    "payment_intent.processing",
    "invoice.payment_succeeded",
    "invoice.payment_failed"
  );

  @Value("${stripe.whsec-account}")
  private String webhookSecret;

  private final MembersRepository membersRepository;

  private final InvoiceRepository invoiceRepository;

  @PostConstruct
  public void clearWebhookEventsDirectory() {
    try {
      Path dir = Paths.get(WEBHOOK_EVENTS_DIR);
      if (Files.exists(dir)) {
        Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .forEach(p -> {
            try {
              Files.delete(p);
            } catch (IOException e) {
              log.warn("Failed to delete {}", p);
            }
          });
        log.info("Cleared webhook events directory");
      }
    } catch (IOException e) {
      log.warn("Failed to clear webhook events directory", e);
    }
  }

  public void process(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid webhook signature", e);
    }


    String eventType = event.getType();
    if (SUPPORTED_EVENTS.contains(eventType)) {
      //writeEventToFile(event);
    }

    switch (eventType) {
      case "setup_intent.created" -> handleSetupIntentCreated(map(event));
      case "setup_intent.succeeded" -> handleSetupIntentSucceeded(map(event));
      case "invoice.created" -> handleInvoiceCreated(map(event), event.getAccount());
      case "payment_intent.processing" -> handlePaymentIntentProcessing(map(event));
      case "invoice.payment_succeeded" -> handlePaymentSucceeded(map(event));
      case "invoice.payment_failed" -> handlePaymentFailed(map(event));
    }

  }

  private void handleSetupIntentCreated(SetupIntent setupIntent) {
    log.info("SetupIntent with id {} of customer {} is created", setupIntent.getId(), setupIntent.getCustomer());
  }

  private void handleSetupIntentSucceeded(SetupIntent setupIntent) {
    log.info("SetupIntent with id {} of customer {} is succeeded", setupIntent.getId(), setupIntent.getCustomer());
    membersRepository.savePaymentMethodId(setupIntent.getCustomer(), setupIntent.getPaymentMethod());
  }

  private void handleInvoiceCreated(Invoice invoice, String accountId) {
    log.info("Creating invoice with id {}...", invoice.getId());
    invoiceRepository.create(invoice);
  }

  private void handlePaymentIntentProcessing(PaymentIntent paymentIntent) {
    log.info("Marking invoice with {} as processing...", paymentIntent.getPaymentDetails().getOrderReference());

    invoiceRepository.markAsProcessing(paymentIntent);
  }

  private void handlePaymentSucceeded(Invoice invoice) {
    log.info("Marking invoice with id {} as paid...", invoice.getId());
    invoiceRepository.markAsPaid(invoice);
  }

  private void handlePaymentFailed(Invoice invoice) {
    //TODO: push notification
    log.info("Marking invoice with id {} failed...", invoice.getId());
    invoiceRepository.markAsFailed(invoice);
  }

  private void writeEventToFile(Event event) {
    try {
      Path dir = Paths.get(WEBHOOK_EVENTS_DIR);
      Files.createDirectories(dir);
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
      String filename = String.format("%s_%s.json", event.getType(), timestamp);
      Path filePath = dir.resolve(filename);
      Files.writeString(filePath, event.toJson());
      log.info("Webhook event written to {}", filePath);
    } catch (IOException e) {
      log.error("Failed to write webhook event to file", e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T map(Event event) {
    return (T) event.getDataObjectDeserializer().getObject().orElseThrow();
  }

}
