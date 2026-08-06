package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.events.InvoicePaidEvent;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.core.repositories.StripeBridge;
import dev.jpitarch.ctrlgym.payments.utils.EpochConverter;
import dev.jpitarch.ctrlgym.payments.utils.MoneyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  private final MembershipsRepository membershipsRepository;

  private final InvoiceRepository invoiceRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final StripeBridge stripeBridge;

  @Value("${stripe.whsec-account}")
  private String webhookSecret;

  @Transactional
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

    /* TODO
    * Ahora se guarda el setup intent id y se guarda en ese preciso momento.
    * Esto lo que hace es confirmar que el IBAN es OK simplemente
    */

  }

  private void handleInvoiceCreated(Invoice invoice) {
    log.info("Creating invoice of member with id {}...", invoice.getId());
    Member.Id memberId = stripeBridge.getId(invoice.getCustomer());

    var inv = dev.jpitarch.ctrlgym.core.domain.Invoice.builder()
      .id(invoice.getId())
      .subtotal(MoneyHelper.toEuros(this.calculateSubtotal(invoice.getTotal())))
      .total(MoneyHelper.toEuros(invoice.getTotal()))
      .currency(invoice.getCurrency())
      .build();

    invoiceRepository.create(inv, memberId);
  }

  private Long calculateSubtotal(Long totalInCents) {
    if (totalInCents == null) return null;

    BigDecimal taxRate = BigDecimal
            .valueOf(dev.jpitarch.ctrlgym.core.domain.Invoice.TAX)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

    BigDecimal subtotal = BigDecimal.valueOf(totalInCents).divide(BigDecimal.ONE.add(taxRate), 0, RoundingMode.HALF_UP);

    return subtotal.longValueExact();
  }

  private void handlePaymentIntentProcessing(PaymentIntent paymentIntent) {
    log.info("Marking invoice with {} as processing...", paymentIntent.getPaymentDetails().getOrderReference());
    invoiceRepository.markAsProcessing(paymentIntent.getPaymentDetails().getOrderReference());
  }

  private void handlePaymentSucceeded(Invoice invoice) {
    log.info("Marking invoice with member with id {} as paid...", invoice.getId());
    invoiceRepository.markAsPaid(invoice.getId());

    var memberId = stripeBridge.getId(invoice.getCustomer());
    var nextBillingDate = EpochConverter.toLocalDate(invoice.getLines().getData().getFirst().getPeriod().getEnd());

    var event = new InvoicePaidEvent(this, invoice.getId(), memberId, nextBillingDate);

    eventPublisher.publishEvent(event);
  }

  private void handlePaymentFailed(Invoice invoice) {
    log.info("Marking invoice with memberId {} failed...", invoice.getId());
    var nextAttempt = EpochConverter.toZonedDateTime(invoice.getNextPaymentAttempt());
    invoiceRepository.markAsFailed(invoice.getId(), nextAttempt);
  }

  @SuppressWarnings("unchecked")
  private <T> T map(Event event) {
    return (T) event.getDataObjectDeserializer().getObject().orElseThrow();
  }


}
