package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
    var requestOptions = RequestOptions.builder()
            .setStripeAccount(props.get("stripeAccountId"))
            .build();

    LocalDate firstDayOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    long billingAnchorTimestamp = firstDayOfNextMonth
            .atStartOfDay(ZoneOffset.UTC)
            .toEpochSecond();

    var customerUpdateParams = CustomerUpdateParams.builder()
            .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                    .setDefaultPaymentMethod(props.get("paymentMethodId"))
                    .build()
            )
            .build();

    Customer.retrieve(props.get("customerId"), requestOptions).update(customerUpdateParams, requestOptions);

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
            .setMetadata(Map.of("gymId", memberId.gymId().toString()))
            .build();

    var subscription = Subscription.create(subscriptionParams, requestOptions);
    return subscription.getId();
  }

  public void change(String subscriptionId, String currentPriceId, String newCurrentPriceId) throws StripeException {
    var subscription = Subscription.retrieve(subscriptionId);
    var schedule = SubscriptionSchedule.create(
            SubscriptionScheduleCreateParams.builder()
                    .setFromSubscription(subscriptionId)
                    .build()
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

    schedule.update(updateParams);
  }

  public void updatePaymentMethod(@Nullable String subscriptionId, String oldPaymentMethodId, String newPaymentMethodId, String stripeAccount) throws StripeException {
    var requestOptions = RequestOptions.builder()
            .setStripeAccount(stripeAccount)
            .build();

    var params = SubscriptionUpdateParams.builder()
            .setDefaultPaymentMethod(newPaymentMethodId)
            .build();

    if (subscriptionId != null) {
      log.info("Updating subscription with id {} payment method from {} to {}...", subscriptionId, oldPaymentMethodId, newPaymentMethodId);
      Subscription.retrieve(subscriptionId, requestOptions).update(params, requestOptions);
    }

// 2. (Alternativa/complemento) Actualizar el default a nivel de customer
    /*CustomerUpdateParams customerParams = CustomerUpdateParams.builder()
            .setInvoiceSettings(
                    CustomerUpdateParams.InvoiceSettings.builder()
                            .setDefaultPaymentMethod(newPaymentMethodId)
                            .build()
            )
            .build();
    customer.update(customerParams);*/

    log.info("Detaching payment method with id {}...", oldPaymentMethodId);

    PaymentMethod.retrieve(oldPaymentMethodId, requestOptions).detach();
  }

  public void cancel(Map<String, String> props) throws StripeException {
    String stripeAccountId = props.get("stripeAccountId");
    String subscriptionId = props.get("subscriptionId");

    var requestOptions = RequestOptions.builder()
            .setStripeAccount(stripeAccountId)
            .build();

    Subscription.retrieve(subscriptionId, requestOptions).cancel();
  }

  private Membership.Recurring mapRecurring(String interval) {
    return switch (interval.toUpperCase()) {
      case "MONTH" -> Membership.Recurring.MONTHLY;
      default -> throw new IllegalStateException("Unexpected value: " + interval);
    };
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
