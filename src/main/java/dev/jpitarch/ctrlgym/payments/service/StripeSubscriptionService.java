package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.SetupIntent;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeSubscriptionService {

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

  public Price createMonthlyPrice() throws StripeException {
    PriceCreateParams params = PriceCreateParams.builder()
      .setCurrency("eur")
      .setUnitAmount(2999L)              // 29.99€
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
          .build()
      )
      .setProductData(
        PriceCreateParams.ProductData.builder()
          .setName("Membresía mensual")
          .build()
      )
      .build();

    return Price.create(params);
  }
}
