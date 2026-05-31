package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final MembershipsRepository membershipsRepository;

  public Price createProduct(GymBranchId gymBranchId, String membershipName, double amount) throws StripeException {

    var stripeAccountId = gymsRepository.getStripeAccountId(gymBranchId.gymId());

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var productParams = ProductCreateParams.builder()
      .setName(membershipName)
      .putMetadata("gymId", String.valueOf(gymBranchId.gymId()))
      .build();

    var product = Product.create(productParams, requestOptions);

    var priceParams = PriceCreateParams.builder()
      .setProduct(product.getId())
      .setCurrency("eur")
      .setUnitAmountDecimal(BigDecimal.valueOf(amount)) //TODO: en Stripe se crea como 0.30€
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
          .build()
      )
      .build();

    var price = Price.create(priceParams, requestOptions);

    membershipsRepository.create(product.getId(), gymBranchId, product.getName(), price.getId(), price.getUnitAmountDecimal().doubleValue(), mapRecurring(price.getRecurring().getInterval()));

    return price;
  }

  public Subscription initializeMembership(String membershipId, UUID memberId, GymBranchId gymBranchId, String paymentMethodId) throws StripeException {

    String stripeAccountId = gymsRepository.getStripeAccountId(gymBranchId.gymId());
    String stripeCustomerId = membersRepository.getStripeCustomerId(memberId);
    String stripePriceId = membershipsRepository.getStripePriceId(membershipId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var paymentMethod = PaymentMethod.retrieve(paymentMethodId, requestOptions);
    paymentMethod.attach(PaymentMethodAttachParams.builder()
        .setCustomer(stripeCustomerId)
        .build(),
      requestOptions
    );

    var customerUpdateParams = CustomerUpdateParams.builder()
      .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
        .setDefaultPaymentMethod(paymentMethodId)
        .build()
      )
      .build();

    Customer.retrieve(stripeCustomerId, requestOptions).update(customerUpdateParams, requestOptions);

    var subscriptionParams = SubscriptionCreateParams.builder()
      .setCustomer(stripeCustomerId)
      .addItem(SubscriptionCreateParams.Item.builder()
        .setPrice(stripePriceId)
        .build()
      )
      .setApplicationFeePercent(new BigDecimal("20.0"))
      .setPaymentSettings(
        SubscriptionCreateParams.PaymentSettings.builder()
          .setPaymentMethodTypes(List.of(SubscriptionCreateParams.PaymentSettings.PaymentMethodType.CARD))
          .build()
      )
      .build();

    var subscription = Subscription.create(subscriptionParams, requestOptions);

    return subscription;
  }

  public SetupIntentResponse createSetupIntent(String customerId) throws StripeException {
    SetupIntentCreateParams params = SetupIntentCreateParams.builder()
      .setCustomer(customerId)
      .addPaymentMethodType("sepa_debit")
      .setMandateData(
        SetupIntentCreateParams.MandateData.builder()
          .setCustomerAcceptance(
            SetupIntentCreateParams.MandateData.CustomerAcceptance.builder()
              .setType(SetupIntentCreateParams.MandateData
                .CustomerAcceptance.Type.ONLINE)
              .build()
          )
          .build()
      )
      .build();

    SetupIntent setupIntent = SetupIntent.create(params);

    // El clientSecret se envía al frontend para mostrar el formulario del IBAN
    return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());
  }

  private Membership.Recurring mapRecurring(String interval) {
    return switch (interval.toUpperCase()) {
      case "MONTH" -> Membership.Recurring.MONTHLY;
      default -> throw new IllegalStateException("Unexpected value: " + interval);
    };
  }

}
