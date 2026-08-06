package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.services.StripeBridge;
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

  @Test
  @DisplayName("create - creates product and price with correct parameters")
  void create_createsProductAndPriceWithCorrectParameters() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class);
         MockedStatic<Price> priceMock = mockStatic(Price.class)) {

      Integer gymId = 1;
      MembershipPlan request = MembershipPlan.builder()
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

      priceMock.when(() -> Price.create(any(PriceCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockPrice);

      String[] result = productService.create(gymId, request);

      assertThat(result).hasSize(2);
      assertThat(result[0]).isEqualTo("prod_test123");
      assertThat(result[1]).isEqualTo("price_test123");

      ArgumentCaptor<ProductCreateParams> productCaptor = ArgumentCaptor.forClass(ProductCreateParams.class);
      productMock.verify(() -> Product.create(productCaptor.capture(), any(RequestOptions.class)));

      ProductCreateParams capturedProductParams = productCaptor.getValue();
      assertThat(capturedProductParams.getName()).isEqualTo("Premium Plan");
      assertThat(capturedProductParams.getMetadata()).extracting("gymId").isEqualTo("1");

      ArgumentCaptor<PriceCreateParams> priceCaptor = ArgumentCaptor.forClass(PriceCreateParams.class);
      priceMock.verify(() -> Price.create(priceCaptor.capture(), any(RequestOptions.class)));

      PriceCreateParams capturedPriceParams = priceCaptor.getValue();
      assertThat(capturedPriceParams.getProduct()).isEqualTo("prod_test123");
      assertThat(capturedPriceParams.getCurrency()).isEqualTo("eur");
      assertThat(capturedPriceParams.getUnitAmountDecimal()).isEqualByComparingTo(new BigDecimal("2999"));
      assertThat(capturedPriceParams.getRecurring().getInterval()).isEqualTo(PriceCreateParams.Recurring.Interval.MONTH);
    }
  }

  @Test
  @DisplayName("create - converts price to cents correctly")
  void create_convertsPriceToCentsCorrectly() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class);
         MockedStatic<Price> priceMock = mockStatic(Price.class)) {

      Integer gymId = 1;
      MembershipPlan request = MembershipPlan.builder()
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
      priceMock.when(() -> Price.create(any(PriceCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockPrice);

      productService.create(gymId, request);

      ArgumentCaptor<PriceCreateParams> priceCaptor = ArgumentCaptor.forClass(PriceCreateParams.class);
      priceMock.verify(() -> Price.create(priceCaptor.capture(), any(RequestOptions.class)));

      assertThat(priceCaptor.getValue().getUnitAmountDecimal()).isEqualByComparingTo(new BigDecimal("1050"));
    }
  }

  @Test
  @DisplayName("create - propagates StripeException")
  void create_propagatesStripeException() throws StripeException {
    try (MockedStatic<Product> productMock = mockStatic(Product.class)) {

      Integer gymId = 1;
      MembershipPlan request = MembershipPlan.builder()
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
      String stripeAccountId = "acct_test123";

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(stripeAccountId);

      Price mockPrice = mock(Price.class);
      Product mockProduct = mock(Product.class);

      priceMock.when(() -> Price.retrieve(eq(productId), any(RequestOptions.class)))
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

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");

      CardException cardException = mock(CardException.class);
      priceMock.when(() -> Price.retrieve(anyString(), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> productService.delete(gymId, productId))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("createTaxRate - creates tax rate with correct parameters")
  void createTaxRate_createsTaxRateWithCorrectParameters() throws StripeException {
    try (MockedStatic<TaxRate> taxRateMock = mockStatic(TaxRate.class)) {

      TaxRate mockTaxRate = mock(TaxRate.class);
      taxRateMock.when(() -> TaxRate.create(any(TaxRateCreateParams.class)))
        .thenReturn(mockTaxRate);

      productService.createTaxRate();

      ArgumentCaptor<TaxRateCreateParams> paramsCaptor = ArgumentCaptor.forClass(TaxRateCreateParams.class);
      taxRateMock.verify(() -> TaxRate.create(paramsCaptor.capture()));

      TaxRateCreateParams capturedParams = paramsCaptor.getValue();
      assertThat(capturedParams.getDisplayName()).isEqualTo("IVA");
      assertThat(capturedParams.getPercentage()).isEqualByComparingTo(new BigDecimal("21"));
      assertThat(capturedParams.getInclusive()).isTrue();
      assertThat(capturedParams.getCountry()).isEqualTo("ES");
      assertThat(capturedParams.getJurisdiction()).isEqualTo("ES");
      assertThat(capturedParams.getDescription()).isEqualTo("IVA español 21%");
    }
  }

  @Test
  @DisplayName("createTaxRate - propagates StripeException")
  void createTaxRate_propagatesStripeException() throws StripeException {
    try (MockedStatic<TaxRate> taxRateMock = mockStatic(TaxRate.class)) {

      CardException cardException = mock(CardException.class);
      taxRateMock.when(() -> TaxRate.create(any(TaxRateCreateParams.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> productService.createTaxRate())
        .isInstanceOf(StripeException.class);
    }
  }
}
