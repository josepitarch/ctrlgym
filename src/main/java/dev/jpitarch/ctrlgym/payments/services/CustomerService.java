package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.StripeBridge;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final StripeBridge stripeBridge;

  public String create(Member member) throws StripeException {
    Integer gymId = member.getId().gymId();

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
          .setLine1(member.getAddress().getStreet())
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

  public SetupIntentResponse createSetupIntent(Member.Id memberId) throws StripeException {
    String accountId = stripeBridge.getStripeAccountId(memberId.gymId());
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

}
