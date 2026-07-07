package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.dto.CreateMembershipPlanRequest;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final GymsRepository gymsRepository;

  public MembershipPlan createProduct(Integer gymId, CreateMembershipPlanRequest request) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var productParams = ProductCreateParams.builder()
      .setName(request.name())
      .putMetadata("gymId", String.valueOf(gymId))
      .build();

    var product = Product.create(productParams, requestOptions);

    var priceParams = PriceCreateParams.builder()
      .setProduct(product.getId())
      .setCurrency("eur")
      .setUnitAmountDecimal(BigDecimal.valueOf(request.price() * 100)) //Stripe trabaja con céntimos
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
          .build()
      )
      .build();

    var price = Price.create(priceParams, requestOptions);
    return MembershipPlan.builder()
      .id(product.getId())
      .name(product.getName())
      .price(price.getUnitAmountDecimal().doubleValue())
      .recurring(mapRecurring(price.getRecurring().getInterval()))
      .stripePriceId(price.getId())
      .build();
  }

  public void deleteProduct(Integer gymId, String productId) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    Product.retrieve(productId, requestOptions).delete(requestOptions);
  }

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
