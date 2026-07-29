package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.StripeBridge;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicesService {

  private final StripeBridge stripeBridge;

  public SetupIntentResponse createSetupIntent(Member.Id memberId) throws StripeException {
    String accountId = stripeBridge.getStripeAccountId(memberId.gymId());
    String customerId = stripeBridge.getStripeCustomerId(memberId).orElseThrow();

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

}
