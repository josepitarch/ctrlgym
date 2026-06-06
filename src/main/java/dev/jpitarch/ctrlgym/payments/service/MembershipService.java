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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final MembershipsRepository membershipsRepository;

  private final CustomerService customerService;

  public void createMembership(GymBranchId gymBranchId, String membershipName, double amount) throws StripeException {

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

    membershipsRepository.createMembershipPlan(product.getId(), gymBranchId, product.getName(), price.getId(), price.getUnitAmountDecimal().doubleValue(), mapRecurring(price.getRecurring().getInterval()));
  }

  public SetupIntentResponse createSetupIntent(UUID memberId) throws StripeException {
    Integer gymId = membersRepository.getGymId(memberId);
    String accountId = gymsRepository.getStripeAccountId(gymId);
    String customerId = membersRepository.getStripeCustomerId(memberId).orElseGet(() -> {
      try {
        return customerService.create(memberId);
      } catch (StripeException e) {
        throw new RuntimeException(e);
      }
    });

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(accountId)
      .build();

    var params = SetupIntentCreateParams.builder()
      .setCustomer(customerId)
      .addPaymentMethodType("sepa_debit")
      .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION) // <- no requiere confirmación del usuario en ese momento. Se cobrará en el futuro
      .build();

    var setupIntent = SetupIntent.create(params, requestOptions);

    return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());
  }

  public void initializeMembership(UUID memberId, String membershipId) throws StripeException {
    if (membershipsRepository.hasMembership(memberId, membershipId)) {
      throw new IllegalStateException("Member " + memberId + " already has membership " + membershipId);
    }

    Integer gymId = membersRepository.getGymId(memberId);
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);
    String stripePriceId = membershipsRepository.getStripePriceId(membershipId);
    Optional<String> paymentMethodId = membersRepository.getPaymentMethodId(memberId);
    Optional<String> customerId = membersRepository.getStripeCustomerId(memberId);

    if (paymentMethodId.isEmpty() || customerId.isEmpty()) {
      throw new IllegalStateException("Customer or payment method not found for member " + memberId);
    }

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    LocalDate firstDayOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    long billingAnchorTimestamp = firstDayOfNextMonth
      .atStartOfDay(ZoneOffset.UTC)
      .toEpochSecond();

    var customerUpdateParams = CustomerUpdateParams.builder()
      .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
        .setDefaultPaymentMethod(paymentMethodId.get())
        .build()
      )
      .build();

    Customer.retrieve(customerId.get(), requestOptions).update(customerUpdateParams, requestOptions);

    var subscriptionParams = SubscriptionCreateParams.builder()
      .setCustomer(customerId.get())
      .addItem(SubscriptionCreateParams.Item.builder()
        .setPrice(stripePriceId)
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
      .build();

    var subscription = Subscription.create(subscriptionParams, requestOptions);
    membershipsRepository.initializeMembership(memberId, membershipId, subscription.getId());
  }

  public void cancelMembership(UUID memberId, String membershipId, Integer cancellationReasonId) throws StripeException {
    if (!membershipsRepository.hasMembership(memberId, membershipId)) {
      throw new IllegalStateException("Membership " + membershipId + " not found for member " + membershipId);
    }

    Integer gymId = membersRepository.getGymId(memberId);
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);
    String subscriptionId = membershipsRepository.getStripeSubscriptionId(memberId, membershipId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var subscription = Subscription.retrieve(subscriptionId, requestOptions);
    subscription.cancel(SubscriptionCancelParams.builder().build(), requestOptions);
    membershipsRepository.cancelMembership(memberId, membershipId, cancellationReasonId);
  }

  private Membership.Recurring mapRecurring(String interval) {
    return switch (interval.toUpperCase()) {
      case "MONTH" -> Membership.Recurring.MONTHLY;
      default -> throw new IllegalStateException("Unexpected value: " + interval);
    };
  }

}
