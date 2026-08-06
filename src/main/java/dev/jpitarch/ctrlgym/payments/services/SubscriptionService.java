package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.payments.utils.EpochConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

  public String create(Member.Id memberId, Map<String, String> props) throws StripeException {
    var options = RequestOptions.builder()
      .setStripeAccount(props.get("stripeAccountId"))
      .build();

    /*
    Esto fuerza que se le cobre prorrateado al usuario y asi se le cobra el 1 de cada mes
    Por ejemplo, si se da alta el día 10 se le cobra en ese momento 20 días restantes del mes
    y al mes siguiente ya es OK
     */
    LocalDate firstDayOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    long billingAnchorTimestamp = firstDayOfNextMonth
      .atStartOfDay(ZoneOffset.UTC)
      .toEpochSecond();

    String paymentMethodId = SetupIntent.retrieve(props.get("setupIntentId"), options).getPaymentMethod();

    var customerUpdateParams = CustomerUpdateParams.builder()
      .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
        .setDefaultPaymentMethod(paymentMethodId)
        .build()
      )
      .build();

    Customer.retrieve(props.get("customerId"), options).update(customerUpdateParams, options);

    var subscriptionParams = SubscriptionCreateParams.builder()
      .setCustomer(props.get("customerId"))
      .addItem(SubscriptionCreateParams.Item.builder()
        .setPrice(props.get("stripePriceId"))
        .build()
      )
      .setApplicationFeePercent(new BigDecimal("0.0"))
      .setPaymentSettings(
        SubscriptionCreateParams.PaymentSettings.builder()
          .setPaymentMethodTypes(List.of(SubscriptionCreateParams.PaymentSettings.PaymentMethodType.CARD))
          .build()
      )
      .setBillingCycleAnchor(billingAnchorTimestamp)
      .setProrationBehavior(SubscriptionCreateParams.ProrationBehavior.CREATE_PRORATIONS)
      .setMetadata(Map.of("gym_id", memberId.gymId().toString()))
      .build();

    var subscription = Subscription.create(subscriptionParams, options);
    return subscription.getId();
  }

  public void change(String subscriptionId, String currentPriceId, String newCurrentPriceId, String stripeAccount) throws StripeException {
    var requestOptions = RequestOptions.builder()
            .setStripeAccount(stripeAccount)
            .build();


    var subscription = Subscription.retrieve(subscriptionId, requestOptions);
    var schedule = SubscriptionSchedule.create(
      SubscriptionScheduleCreateParams.builder()
        .setFromSubscription(subscriptionId)
        .build(),
      requestOptions
    );

    var updateParams = SubscriptionScheduleUpdateParams.builder()
      .addPhase(SubscriptionScheduleUpdateParams.Phase.builder()
        .addItem(SubscriptionScheduleUpdateParams.Phase.Item.builder()
          .setPrice(currentPriceId)
          .build())
        .setEndDate(subscription.getItems().getData().getFirst().getCurrentPeriodEnd())
        .build())
      .addPhase(SubscriptionScheduleUpdateParams.Phase.builder()
        .addItem(SubscriptionScheduleUpdateParams.Phase.Item.builder()
          .setPrice(newCurrentPriceId)
          .build())
        .build())
      .build();

    schedule.update(updateParams, requestOptions);
  }

  public LocalDate cancel(Map<String, String> props) throws StripeException {
    String stripeAccountId = props.get("stripeAccountId");
    String subscriptionId = props.get("subscriptionId");

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var subscription = Subscription.retrieve(subscriptionId, options);

    var params = SubscriptionUpdateParams.builder()
      .setCancelAtPeriodEnd(true)
      .build();

    subscription.update(params, options);

    return EpochConverter.toLocalDate(subscription.getItems().getData().getFirst().getCurrentPeriodEnd());
  }


  public LocalDate getNextBillingDate(String subscriptionId, String stripeAccountId) throws StripeException {
    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var subscription = Subscription.retrieve(subscriptionId, options);
    return EpochConverter.toLocalDate(subscription.getItems().getData().getFirst().getCurrentPeriodEnd());
  }

  public void createTaxRate() throws StripeException {
    var taxRateParams = TaxRateCreateParams.builder()
      .setDisplayName("IVA")
      .setPercentage(new BigDecimal("21"))
      .setInclusive(true)
      .setCountry("ES")
      .setJurisdiction("ES")
      .setDescription("IVA español 21%")
      .build();

    TaxRate.create(taxRateParams);
  }

}
