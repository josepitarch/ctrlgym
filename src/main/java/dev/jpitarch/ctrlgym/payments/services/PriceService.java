package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.net.RequestOptions;
import com.stripe.param.PriceCreateParams;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

  private final StripeBridge stripeBridge;

  public Price createPrice(String productId, Double price) throws StripeException {
    String stripeAccountId = stripeBridge.getStripeAccountId(TenantContextHolder.getTenantId());

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var priceParams = PriceCreateParams.builder()
      .setProduct(productId)
      .setCurrency("eur")
      .setUnitAmountDecimal(BigDecimal.valueOf(price * 100)) //Stripe works with cents
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
          .build()
      )
      .build();

    log.info("Creating price for product with id {} with amount {}...", productId, price);

    return Price.create(priceParams, options);
  }
}
