package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.models.InvoiceMO;
import dev.jpitarch.ctrlgym.payments.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  @Value("${stripe.whsec-account}")
  private String webhookSecret;

  private final MembersRepository membersRepository;

  private final InvoiceRepository invoiceRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Retryable(delay = 500, maxRetries = 3)
  public void process(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid webhook signature", e);
    }

    switch (event.getType()) {
      case "setup_intent.created" -> handleSetupIntentCreated(map(event));
      case "setup_intent.succeeded" -> handleSetupIntentSucceeded(map(event));
      case "invoice.finalized" -> handleInvoiceCreated(map(event), event.getAccount());
      case "payment_intent.processing" -> handlePaymentIntentProcessing(map(event));
      case "invoice.payment_succeeded" -> handlePaymentSucceeded(map(event));
      case "invoice.payment_failed" -> handlePaymentFailed(map(event));
      case "customer.subscription.updated" -> handleSubscriptionUpdated(map(event));
    }

  }

  private void handleSubscriptionUpdated(Subscription subscription) {
    //Aquí manejaremos cuándo un miembro decide cambiar de membresía
  }

  private void handleSetupIntentCreated(SetupIntent setupIntent) {
    log.info("SetupIntent of member with id {} of customer {} is created", setupIntent.getId(), setupIntent.getCustomer());
  }

  private void handleSetupIntentSucceeded(SetupIntent setupIntent) {
    log.info("SetupIntent of member with id {} of customer {} is succeeded", setupIntent.getId(), setupIntent.getCustomer());
    //TODO: si ya tenía en método de pago hay que hacerle un detach para desvincularlo del Customer
    membersRepository.savePaymentMethodId(setupIntent.getCustomer(), setupIntent.getPaymentMethod());
  }

  private void handleInvoiceCreated(Invoice invoice, String accountId) {
    log.info("Creating invoice of member with id {}...", invoice.getId());
    invoiceRepository.create(invoice, accountId);
  }

  private void handlePaymentIntentProcessing(PaymentIntent paymentIntent) {
    log.info("Marking invoice with {} as processing...", paymentIntent.getPaymentDetails().getOrderReference());
    invoiceRepository.markAsProcessing(paymentIntent);
  }

  private void handlePaymentSucceeded(Invoice invoice) {
    log.info("Marking invoice with member with id {} as paid...", invoice.getId());
    invoiceRepository.markAsPaid(invoice);

    //TODO: setear next_billing_date en función del Recurring

    dev.jpitarch.ctrlgym.core.domain.Invoice inv = invoiceRepository
      .getInvoice(invoice.getId())
      .orElseThrow(() -> new IllegalArgumentException("Invoice with memberId " + invoice.getId() + " does not exist"));

    //TODO: retrieve name, surnames and nif
    eventPublisher.publishEvent(inv);
  }

  private void handlePaymentFailed(Invoice invoice) {
    //TODO: push notification
    log.info("Marking invoice with memberId {} failed...", invoice.getId());
    invoiceRepository.markAsFailed(invoice);
  }

  @SuppressWarnings("unchecked")
  private <T> T map(Event event) {
    return (T) event.getDataObjectDeserializer().getObject().orElseThrow();
  }


}
