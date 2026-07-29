package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  private final SubscriptionService subscriptionService;

  private final MembersRepository membersRepository;

  private final MembershipsRepository membershipsRepository;

  private final InvoiceRepository invoiceRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final StripeBridge stripeBridge;

  @Value("${stripe.whsec-account}")
  private String webhookSecret;

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
      case "setup_intent.succeeded" -> handleSetupIntentSucceeded(map(event));
      case "invoice.finalized" -> handleInvoiceCreated(map(event));
      case "payment_intent.processing" -> handlePaymentIntentProcessing(map(event));
      case "invoice.payment_succeeded" -> handlePaymentSucceeded(map(event));
      case "invoice.payment_failed" -> handlePaymentFailed(map(event));
      case "customer.subscription.updated" -> handleSubscriptionUpdated(map(event));
    }

  }

  private void handleSubscriptionUpdated(Subscription subscription) {
    String product = subscription.getItems().getData().getFirst().getPrice().getProduct();
    Long membershipId = membershipsRepository.getIdByStripeSubscriptionId(product);

    log.info("Setting membership with id {} to plan {}", membershipId, product);
    membershipsRepository.setMembershipPlanId(membershipId, product);
  }

  private void handleSetupIntentSucceeded(SetupIntent setupIntent) {
    log.info("SetupIntent of member with id {} of customer {} is succeeded", setupIntent.getId(), setupIntent.getCustomer());

    var memberId = stripeBridge.getId(setupIntent.getCustomer());

    stripeBridge.getPaymentMethodId(setupIntent.getCustomer()).ifPresent(pm -> {
      try {
        var subscriptionId = membershipsRepository.getStripeSubscriptionId(memberId);
        var stripeAccount = stripeBridge.getStripeAccountId(memberId.gymId());

        log.info("Member with id {} has already a payment method configured. Updating...", memberId);

        subscriptionService.updatePaymentMethod(subscriptionId, pm, setupIntent.getPaymentMethod(), stripeAccount);
      } catch (StripeException e) {
        throw new RuntimeException(e);
      }
    });

    log.info("Saving payment method with id {} for member with id {}...", memberId, setupIntent.getPaymentMethod());
    membersRepository.savePaymentMethodId(setupIntent.getCustomer(), setupIntent.getPaymentMethod());
  }

  private void handleInvoiceCreated(Invoice invoice) {
    log.info("Creating invoice of member with id {}...", invoice.getId());
    Member.Id memberId = stripeBridge.getId(invoice.getCustomer());
    var inv = dev.jpitarch.ctrlgym.core.domain.Invoice.builder()
      .id(invoice.getId())
      .subtotal(BigDecimal.valueOf(invoice.getSubtotal()))
      .total(BigDecimal.valueOf(invoice.getTotal()))
      .currency(invoice.getCurrency())
      .build();
    invoiceRepository.create(inv, memberId);
  }

  private void handlePaymentIntentProcessing(PaymentIntent paymentIntent) {
    log.info("Marking invoice with {} as processing...", paymentIntent.getPaymentDetails().getOrderReference());
    invoiceRepository.markAsProcessing(paymentIntent.getPaymentDetails().getOrderReference());
  }

  private void handlePaymentSucceeded(Invoice invoice) {
    log.info("Marking invoice with member with id {} as paid...", invoice.getId());
    invoiceRepository.markAsPaid(invoice.getId());

    //TODO: setear next_billing_date en función del Recurring
    long nextChargeDate = invoice.getLines().getData().getFirst().getPeriod().getEnd();
    LocalDate localDate = Instant.ofEpochSecond(nextChargeDate).atZone(ZoneId.of("Europe/Madrid")).toLocalDate();

    dev.jpitarch.ctrlgym.core.domain.Invoice inv = invoiceRepository
      .getInvoice(invoice.getId())
      .orElseThrow(() -> new IllegalArgumentException("Invoice with memberId " + invoice.getId() + " does not exist"));

    if (invoice.getParent() != null
      && "subscription_details".equals(invoice.getParent().getType())) {
      String subscriptionId = invoice.getParent()
        .getSubscriptionDetails()
        .getSubscription();
    }

    var member = membersRepository.getById(stripeBridge.getId(invoice.getCustomer()));
    inv.setName(member.getName());
    inv.setFirstSurname(member.getFirstSurname());
    inv.setSecondSurname(member.getSecondSurname());
    inv.setNif(member.getNif());

    eventPublisher.publishEvent(inv);
  }

  private void handlePaymentFailed(Invoice invoice) {
    //TODO: push notification
    log.info("Marking invoice with memberId {} failed...", invoice.getId());
    invoiceRepository.markAsFailed(invoice.getId());
  }

  @SuppressWarnings("unchecked")
  private <T> T map(Event event) {
    return (T) event.getDataObjectDeserializer().getObject().orElseThrow();
  }


}
