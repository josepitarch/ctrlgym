package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import io.opentelemetry.sdk.logs.LogLimits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final PriceService priceService;

  private final StripeBridge stripeBridge;

  public String[] create(Integer gymId, MembershipPlan request) throws StripeException {
    String stripeAccountId = stripeBridge.getStripeAccountId(gymId);

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var productParams = ProductCreateParams.builder()
      .setName(request.getName())
      .putMetadata("gym_id", String.valueOf(gymId))
      .build();

    log.info("Creating product for gym with id {} with name {}...", gymId, request.getName());

    var product = Product.create(productParams, options);

    var price = priceService.createPrice(product.getId(), request.getPrice());

    return new String[]{product.getId(), price.getId()};
  }

  public String changePrice(String productId, Double price) throws StripeException {
    log.info("Changing price of product with {} to {} €...", productId, price);
    return priceService.createPrice(productId, price).getId();
  }


  public void changePrice1(String subscriptionId, Double priceId) throws StripeException {
    var subscription = Subscription.retrieve(subscriptionId);

    if (subscription.getSchedule() != null) {
      throw new IllegalStateException("La suscripción " + subscriptionId + " ya tiene un schedule activo: " + subscription.getSchedule());
    }

    SubscriptionItem currentItem = subscription.getItems().getData().getFirst();
    String currentPriceId = currentItem.getPrice().getId();
    Long currentPeriodEnd = subscription.getItems().getData().getFirst().getCurrentPeriodEnd();

    var schedule = SubscriptionSchedule.create(
      SubscriptionScheduleCreateParams.builder()
        .setFromSubscription(subscriptionId)
        .build()
    );

    var params = SubscriptionScheduleUpdateParams.builder()
      .addPhase(
        SubscriptionScheduleUpdateParams.Phase.builder()
          .addItem(
            SubscriptionScheduleUpdateParams.Phase.Item.builder()
              .setPrice(currentPriceId)
              .setQuantity(currentItem.getQuantity())
              .build()
          )
          .setStartDate(subscription.getItems().getData().getFirst().getCurrentPeriodStart())
          .setEndDate(currentPeriodEnd)
          .build()
      )
      .addPhase(
        SubscriptionScheduleUpdateParams.Phase.builder()
          .addItem(
            SubscriptionScheduleUpdateParams.Phase.Item.builder()
              .setPrice(priceId.toString())
              .setQuantity(currentItem.getQuantity())
              .build()
          )
          .setProrationBehavior(SubscriptionScheduleUpdateParams.Phase.ProrationBehavior.NONE)
          .build()
      )
      .build();

    schedule.update(params);
  }

  public void delete(Integer gymId, String productId) throws StripeException {
    String stripeAccountId = stripeBridge.getStripeAccountId(gymId);
    String priceId = stripeBridge.getStripePriceId(productId);

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();

    var priceParams = PriceUpdateParams.builder()
      .setActive(false)
      .build();

    log.info("Deleting price for gym with id {} with product with id {}...", gymId, productId);

    Price.retrieve(priceId, options).update(priceParams, options);

    var productParams = ProductUpdateParams.builder()
      .setActive(false)
      .build();

    log.info("Deleting product for gym with id {} with product with id {}...", gymId, productId);

    Product.retrieve(productId, options).update(productParams, options);
  }

}
