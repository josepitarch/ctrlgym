package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerRetrieveParams;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import dev.jpitarch.ctrlgym.payments.dtos.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final StripeBridge stripeBridge;

  public String create(UUID memberId, String email, String fullName, String nif) throws StripeException {
    Integer gymId = TenantContextHolder.getTenantId();

    var options = RequestOptions.builder()
      .setStripeAccount(stripeBridge.getStripeAccountId(gymId))
      .build();

    var params = CustomerCreateParams.builder()
      .setName(fullName)
      .setEmail(email)
      .setMetadata(Map.of(
        "nif", nif,
        "gym_id", gymId.toString()
      ))
      .build();

    log.info("Creating a customer with member with id {}...", memberId);
    var customer = Customer.create(params, options);

    return customer.getId();
  }

  public SetupIntentResponse createSetupIntent(UUID memberId) throws StripeException {
    String accountId = stripeBridge.getStripeAccountId(TenantContextHolder.getTenantId());
    String customerId = stripeBridge.getStripeCustomerId(memberId).orElseThrow();

    var options = RequestOptions.builder()
      .setStripeAccount(accountId)
      .build();

    var params = SetupIntentCreateParams.builder()
      .setCustomer(customerId)
      .addPaymentMethodType("sepa_debit")
      .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
      .build();

    log.info("Creating a setup intent for member with id {}...", memberId);

    var setupIntent = SetupIntent.create(params, options);

    stripeBridge.saveStripeSetupIntentId(memberId, setupIntent.getId());

    return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());
  }

  public Optional<String> getIbanLast4(UUID memberId) {
    try {
      var options = RequestOptions.builder()
        .setStripeAccount(stripeBridge.getStripeAccountId(TenantContextHolder.getTenantId()))
        .build();

      var customerParams = CustomerRetrieveParams.builder()
        .addExpand("invoice_settings.default_payment_method")
        .build();

      var customer = Customer.retrieve(stripeBridge.getStripeCustomerId(memberId).orElseThrow(), customerParams, options);

      PaymentMethod pm = customer.getInvoiceSettings().getDefaultPaymentMethodObject();

      return Optional.ofNullable(pm).map(PaymentMethod::getSepaDebit).map(PaymentMethod.SepaDebit::getLast4);
    } catch (StripeException e) {
      log.error("Error retrieving IBAN last 4 for member with id {}: {}", memberId, e.getMessage(), e);
      return Optional.empty();
    }
  }

  public boolean setupIntentIsValid(String setupIntentId) throws StripeException {
    var options = RequestOptions.builder()
      .setStripeAccount(stripeBridge.getStripeAccountId(TenantContextHolder.getTenantId()))
      .build();

    String status = SetupIntent.retrieve(setupIntentId, options).getStatus();
    log.debug("SetupIntent with id {} has status {}", setupIntentId, status);
    return "processing".equals(status) || "succeeded".equals(status);

  }

}
