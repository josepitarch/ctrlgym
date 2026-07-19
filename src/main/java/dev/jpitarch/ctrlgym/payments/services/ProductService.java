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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final GymsRepository gymsRepository;

  public MembershipPlan create(Integer gymId, CreateMembershipPlanRequest request) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var productParams = ProductCreateParams.builder()
      .setName(request.name())
      .putMetadata("gymId", String.valueOf(gymId))
      .build();

    log.info("Creating product for gym with id {} with name {}", gymId, request.name());

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

    log.info("Creating price for product with id {} with amount {}", product.getId(), request.price());

    var price = Price.create(priceParams, requestOptions);

   //TODO: aquí solo devolver los ids de stripe. La instancia de un objeto de dominio ha de estar en el core
    return MembershipPlan.builder()
      .id(product.getId())
      .name(product.getName())
      .price(price.getUnitAmountDecimal().doubleValue())
      .recurring(mapRecurring(price.getRecurring().getInterval()))
      .stripePriceId(price.getId())
      .build();
  }

  public void delete(Integer gymId, String productId) throws StripeException {
    String stripeAccountId = gymsRepository.getStripeAccountId(gymId);

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    log.info("Deleting product for gym wit id {} with product with id {}", gymId, productId);

    var priceParams = PriceUpdateParams.builder()
      .setActive(false)
      .build();

    Price.retrieve(productId, requestOptions).update(priceParams, requestOptions);

    var productParams = ProductUpdateParams.builder()
      .setActive(false)
      .build();

    Product.retrieve(productId, requestOptions).update(productParams, requestOptions);
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
