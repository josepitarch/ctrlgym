package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.TaxRate;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final GymsRepository gymsRepository;

  public String[] create(Integer gymId, MembershipPlan request) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var productParams = ProductCreateParams.builder()
      .setName(request.getName())
      .putMetadata("gymId", String.valueOf(gymId))
      .build();

    log.info("Creating product for gym with id {} with name {}", gymId, request.getName());

    var product = Product.create(productParams, options);

    var priceParams = PriceCreateParams.builder()
      .setProduct(product.getId())
      .setCurrency("eur")
      .setUnitAmountDecimal(BigDecimal.valueOf(request.getPrice() * 100)) //Stripe works with cents
      .setRecurring(
        PriceCreateParams.Recurring.builder()
          .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
          .build()
      )
      .build();

    log.info("Creating price for product with id {} with amount {}", product.getId(), request.getPrice());

    var price = Price.create(priceParams, options);

    return new String[]{ product.getId(), price.getId() };
  }

  public void delete(Integer gymId, String productId) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    log.info("Deleting product for gym with id {} with product with id {}...", gymId, productId);

    var priceParams = PriceUpdateParams.builder()
      .setActive(false)
      .build();

    Price.retrieve(productId, options).update(priceParams, options);

    var productParams = ProductUpdateParams.builder()
      .setActive(false)
      .build();

    Product.retrieve(productId, options).update(productParams, options);
  }

  private MembershipPlan.Recurring mapRecurring(String interval) {
    return switch (interval.toUpperCase()) {
      case "MONTH" -> MembershipPlan.Recurring.MONTHLY;
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
