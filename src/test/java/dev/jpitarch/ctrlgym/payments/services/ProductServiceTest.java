package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks
  ProductService productService;

  @Mock
  StripeBridge stripeBridge;

  @Mock
  PriceService priceService;

  @Test
  @DisplayName("create - creates product and price with correct parameters")
  void create_createsProductAndPriceWithCorrectParameters() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class)) {

      Integer gymId = 1;
      var request = MembershipPlan.builder()
        .name("Premium Plan")
        .price(29.99)
        .build();

      String stripeAccountId = "acct_test123";
      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(stripeAccountId);

      Product mockProduct = mock(Product.class);
      when(mockProduct.getId()).thenReturn("prod_test123");

      Price mockPrice = mock(Price.class);
      when(mockPrice.getId()).thenReturn("price_test123");

      productMock.when(() -> Product.create(any(ProductCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockProduct);

      when(priceService.createPrice(eq("prod_test123"), eq(29.99))).thenReturn(mockPrice);

      String[] result = productService.create(gymId, request);

      assertThat(result).hasSize(2);
      assertThat(result[0]).isEqualTo("prod_test123");
      assertThat(result[1]).isEqualTo("price_test123");

      ArgumentCaptor<ProductCreateParams> productCaptor = ArgumentCaptor.forClass(ProductCreateParams.class);
      productMock.verify(() -> Product.create(productCaptor.capture(), any(RequestOptions.class)));

      ProductCreateParams capturedProductParams = productCaptor.getValue();
      assertThat(capturedProductParams.getName()).isEqualTo("Premium Plan");
      assertThat(capturedProductParams.getMetadata()).extracting("gym_id").isEqualTo("1");

      verify(priceService).createPrice(eq("prod_test123"), eq(29.99));
    }
  }

  @Test
  @DisplayName("create - delegates price creation to PriceService")
  void create_delegatesPriceCreationToPriceService() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class)) {

      Integer gymId = 1;
      var request = MembershipPlan.builder()
        .name("Basic Plan")
        .price(10.50)
        .build();

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");

      Product mockProduct = mock(Product.class);
      when(mockProduct.getId()).thenReturn("prod_test");

      Price mockPrice = mock(Price.class);
      when(mockPrice.getId()).thenReturn("price_test");

      productMock.when(() -> Product.create(any(ProductCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockProduct);

      when(priceService.createPrice(eq("prod_test"), eq(10.50))).thenReturn(mockPrice);

      productService.create(gymId, request);

      verify(priceService).createPrice(eq("prod_test"), eq(10.50));
    }
  }

  @Test
  @DisplayName("create - propagates StripeException")
  void create_propagatesStripeException() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class)) {

      Integer gymId = 1;
      var request = MembershipPlan.builder()
        .name("Test Plan")
        .price(10.0)
        .build();

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");

      CardException cardException = mock(CardException.class);
      productMock.when(() -> Product.create(any(ProductCreateParams.class), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> productService.create(gymId, request))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("delete - deactivates price and product")
  void delete_deactivatesPriceAndProduct() throws StripeException {
    try (MockedStatic<Price> priceMock = mockStatic(Price.class);
         MockedStatic<Product> productMock = mockStatic(Product.class)) {

      Integer gymId = 1;
      String productId = "prod_test123";
      String priceId = "price_test123";
      String stripeAccountId = "acct_test123";

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(stripeAccountId);
      when(stripeBridge.getStripePriceId(productId)).thenReturn(priceId);

      Price mockPrice = mock(Price.class);
      Product mockProduct = mock(Product.class);

      priceMock.when(() -> Price.retrieve(eq(priceId), any(RequestOptions.class)))
        .thenReturn(mockPrice);
      when(mockPrice.update(any(PriceUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockPrice);

      productMock.when(() -> Product.retrieve(eq(productId), any(RequestOptions.class)))
        .thenReturn(mockProduct);
      when(mockProduct.update(any(ProductUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockProduct);

      productService.delete(gymId, productId);

      ArgumentCaptor<PriceUpdateParams> priceCaptor = ArgumentCaptor.forClass(PriceUpdateParams.class);
      verify(mockPrice).update(priceCaptor.capture(), any(RequestOptions.class));
      assertThat(priceCaptor.getValue().getActive()).isFalse();

      ArgumentCaptor<ProductUpdateParams> productCaptor = ArgumentCaptor.forClass(ProductUpdateParams.class);
      verify(mockProduct).update(productCaptor.capture(), any(RequestOptions.class));
      assertThat(productCaptor.getValue().getActive()).isFalse();
    }
  }

  @Test
  @DisplayName("delete - propagates StripeException")
  void delete_propagatesStripeException() throws StripeException {
    try (MockedStatic<Price> priceMock = mockStatic(Price.class)) {

      Integer gymId = 1;
      String productId = "prod_test";
      String priceId = "price_test";

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
      when(stripeBridge.getStripePriceId(productId)).thenReturn(priceId);

      CardException cardException = mock(CardException.class);
      priceMock.when(() -> Price.retrieve(eq(priceId), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> productService.delete(gymId, productId))
        .isInstanceOf(StripeException.class);
    }
  }

}
