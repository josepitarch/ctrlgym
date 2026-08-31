package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.SetupIntentRetrieveParams;
import com.stripe.param.SubscriptionUpdateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import dev.jpitarch.ctrlgym.payments.dtos.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final StripeBridge stripeBridge;

  public String create(Member member) throws StripeException {
    Integer gymId = TenantContextHolder.getTenantId();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeBridge.getStripeAccountId(gymId))
      .build();

    var params = CustomerCreateParams.builder()
      .setName(member.getFullName())
      .setEmail(member.getEmail())
      /*.addTaxIdData(CustomerCreateParams.TaxIdData.builder()
        .setType(CustomerCreateParams.TaxIdData.Type.ES_CIF)
        .setValue("45911747K")
        .build()
      )*/
      .setAddress(
        CustomerCreateParams.Address.builder()
          .setCity(member.getAddress().getCity())
          .setPostalCode(member.getAddress().getPostalCode().toString())
          .setCountry("ES")
          .build()
      )
      .setMetadata(Map.of(
        "nif", member.getNif(),
        "gym_id", gymId.toString()
      ))
      .build();

    log.info("Creating a customer with member with id {}...", member.getId());
    var customer = Customer.create(params, requestOptions);

    return customer.getId();
  }

  public SetupIntentResponse createSetupIntent(UUID memberId, Integer gymId) throws StripeException {
    String accountId = stripeBridge.getStripeAccountId(gymId);
    String customerId = stripeBridge.getStripeCustomerId(memberId).orElseThrow();

    var options = RequestOptions.builder()
      .setStripeAccount(accountId)
      .build();

    var params = SetupIntentCreateParams.builder()
      .setCustomer(customerId)
      .addPaymentMethodType("sepa_debit")
      .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
      .build();

    var setupIntent = SetupIntent.create(params, options);

    stripeBridge.saveStripeSetupIntentId(memberId, setupIntent.getId());

    return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());
  }

  public void updateSetupIntentId(@Nullable String subscriptionId, String oldSetupIntentId, String newPaymentMethodId, String stripeAccount) throws StripeException {
    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccount)
      .build();

    var params = SubscriptionUpdateParams.builder()
      .setDefaultPaymentMethod(newPaymentMethodId)
      .build();

    String oldPaymentMethodId = SetupIntent.retrieve(oldSetupIntentId, options).getPaymentMethod();

    if (subscriptionId != null) {
      log.info("Updating subscription with id {} payment method from {} to {}...", subscriptionId, oldPaymentMethodId, newPaymentMethodId);
      Subscription.retrieve(subscriptionId, options).update(params, options);
    }

    log.info("Detaching payment method with id {}...", oldPaymentMethodId);

    PaymentMethod.retrieve(oldPaymentMethodId, options).detach(options);
  }

  public Optional<String> getIbanLast4(UUID memberId) {
    var options = RequestOptions.builder()
      .setStripeAccount(stripeBridge.getStripeAccountId(TenantContextHolder.getTenantId()))
      .build();
    var params = SetupIntentRetrieveParams.builder()
      .addExpand("payment_method")
      .build();

    return stripeBridge.getStripeSetupIntentId(memberId)
      .flatMap(s -> {
        try {
          return Optional.ofNullable(
            SetupIntent.retrieve(s, params, options).getPaymentMethodObject().getSepaDebit().getLast4()
          );
        } catch (StripeException e) {
          log.warn("Failed to retrieve IBAN last4 for member {}: {}", memberId, e.getMessage(), e);
          return Optional.empty();
        }
      });

  }

}
