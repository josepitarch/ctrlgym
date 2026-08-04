package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import dev.jpitarch.ctrlgym.core.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @InjectMocks
  SubscriptionService subscriptionService;

  private final Member.Id memberId = new Member.Id(UUID.randomUUID(), 1);

  @Test
  @DisplayName("create - creates subscription with correct parameters")
  void create_createsSubscriptionWithCorrectParameters() throws StripeException {
    try (MockedStatic<Customer> customerMock = mockStatic(Customer.class);
         MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class);
         MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      Map<String, String> props = Map.of(
        "stripeAccountId", "acct_test123",
        "setupIntentId", "seti_test123",
        "customerId", "cus_test123",
        "stripePriceId", "price_test123"
      );

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getPaymentMethod()).thenReturn("pm_test123");

      setupIntentMock.when(() -> SetupIntent.retrieve(eq("seti_test123"), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      Customer mockCustomer = mock(Customer.class);
      Subscription mockSubscription = mock(Subscription.class);

      customerMock.when(() -> Customer.retrieve(eq("cus_test123"), any(RequestOptions.class)))
        .thenReturn(mockCustomer);
      when(mockCustomer.update(any(CustomerUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockCustomer);

      subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSubscription);
      when(mockSubscription.getId()).thenReturn("sub_test123");

      String result = subscriptionService.create(memberId, props);

      assertThat(result).isEqualTo("sub_test123");

      ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionCreateParams.class);
      subscriptionMock.verify(() -> Subscription.create(paramsCaptor.capture(), any(RequestOptions.class)));

      SubscriptionCreateParams capturedParams = paramsCaptor.getValue();
      assertThat(capturedParams.getCustomer()).isEqualTo("cus_test123");
      assertThat(capturedParams.getApplicationFeePercent().toString()).isEqualTo("0.0");
      assertThat(capturedParams.getBillingCycleAnchor()).isNotNull();
      assertThat(capturedParams.getProrationBehavior()).isEqualTo(SubscriptionCreateParams.ProrationBehavior.CREATE_PRORATIONS);
      assertThat(capturedParams.getMetadata()).extracting("gym_id").isEqualTo(memberId.gymId().toString());
    }
  }

  @Test
  @DisplayName("create - sets billing anchor to first day of next month")
  void create_setsBillingAnchorToFirstDayOfNextMonth() throws StripeException {
    try (MockedStatic<Customer> customerMock = mockStatic(Customer.class);
         MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class);
         MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class);
         MockedStatic<LocalDate> localDateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {

      LocalDate today = LocalDate.of(2026, 7, 15);
      localDateMock.when(LocalDate::now).thenReturn(today);

      Map<String, String> props = Map.of(
        "stripeAccountId", "acct_test",
        "setupIntentId", "seti_test",
        "customerId", "cus_test",
        "stripePriceId", "price_test"
      );

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getPaymentMethod()).thenReturn("pm_test");

      setupIntentMock.when(() -> SetupIntent.retrieve(anyString(), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      Customer mockCustomer = mock(Customer.class);
      Subscription mockSubscription = mock(Subscription.class);

      customerMock.when(() -> Customer.retrieve(anyString(), any(RequestOptions.class)))
        .thenReturn(mockCustomer);
      when(mockCustomer.update(any(CustomerUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockCustomer);

      subscriptionMock.when(() -> Subscription.create(any(SubscriptionCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSubscription);
      when(mockSubscription.getId()).thenReturn("sub_test");

      subscriptionService.create(memberId, props);

      ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionCreateParams.class);
      subscriptionMock.verify(() -> Subscription.create(paramsCaptor.capture(), any(RequestOptions.class)));

      long expectedTimestamp = LocalDate.of(2026, 8, 1)
        .atStartOfDay(ZoneOffset.UTC)
        .toEpochSecond();

      assertThat(paramsCaptor.getValue().getBillingCycleAnchor()).isEqualTo(expectedTimestamp);
    }
  }

  @Test
  @DisplayName("create - propagates StripeException")
  void create_propagatesStripeException() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      Map<String, String> props = Map.of(
        "stripeAccountId", "acct_test",
        "setupIntentId", "seti_test",
        "customerId", "cus_test",
        "stripePriceId", "price_test"
      );

      CardException cardException = mock(CardException.class);
      setupIntentMock.when(() -> SetupIntent.retrieve(anyString(), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> subscriptionService.create(memberId, props))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("change - creates subscription schedule with two phases")
  void change_createsSubscriptionScheduleWithTwoPhases() throws StripeException {
    try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class);
         MockedStatic<SubscriptionSchedule> scheduleMock = mockStatic(SubscriptionSchedule.class)) {

      String subscriptionId = "sub_test123";
      String currentPriceId = "price_current";
      String newPriceId = "price_new";
      String stripeAccount = "acct_test";

      Subscription mockSubscription = mock(Subscription.class);
      SubscriptionSchedule mockSchedule = mock(SubscriptionSchedule.class);
      SubscriptionItem mockItem = mock(SubscriptionItem.class);

      subscriptionMock.when(() -> Subscription.retrieve(eq(subscriptionId), any(RequestOptions.class)))
        .thenReturn(mockSubscription);
      when(mockSubscription.getItems()).thenReturn(mock(SubscriptionItemCollection.class));
      when(mockSubscription.getItems().getData()).thenReturn(List.of(mockItem));
      when(mockItem.getCurrentPeriodEnd()).thenReturn(1735689600L);

      scheduleMock.when(() -> SubscriptionSchedule.create(any(SubscriptionScheduleCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSchedule);
      when(mockSchedule.update(any(SubscriptionScheduleUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSchedule);

      subscriptionService.change(subscriptionId, currentPriceId, newPriceId, stripeAccount);

      ArgumentCaptor<SubscriptionScheduleCreateParams> createCaptor = ArgumentCaptor.forClass(SubscriptionScheduleCreateParams.class);
      scheduleMock.verify(() -> SubscriptionSchedule.create(createCaptor.capture(), any(RequestOptions.class)));
      assertThat(createCaptor.getValue().getFromSubscription()).isEqualTo(subscriptionId);

      ArgumentCaptor<SubscriptionScheduleUpdateParams> updateCaptor = ArgumentCaptor.forClass(SubscriptionScheduleUpdateParams.class);
      verify(mockSchedule).update(updateCaptor.capture(), any(RequestOptions.class));

      SubscriptionScheduleUpdateParams capturedParams = updateCaptor.getValue();
      assertThat(capturedParams.getPhases()).hasSize(2);
      assertThat(capturedParams.getPhases().get(0).getItems().get(0).getPrice()).isEqualTo(currentPriceId);
      assertThat(capturedParams.getPhases().get(1).getItems().get(0).getPrice()).isEqualTo(newPriceId);
    }
  }

  @Test
  @DisplayName("change - propagates StripeException")
  void change_propagatesStripeException() throws StripeException {
    try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {

      CardException cardException = mock(CardException.class);
      subscriptionMock.when(() -> Subscription.retrieve(anyString(), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> subscriptionService.change("sub_test", "price_old", "price_new", "acct_test"))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("cancel - cancels subscription with correct parameters")
  void cancel_cancelsSubscriptionWithCorrectParameters() throws StripeException {
    try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {

      Map<String, String> props = Map.of(
        "stripeAccountId", "acct_test123",
        "subscriptionId", "sub_test123"
      );

      Subscription mockSubscription = mock(Subscription.class);

      subscriptionMock.when(() -> Subscription.retrieve(eq("sub_test123"), any(RequestOptions.class)))
        .thenReturn(mockSubscription);
      when(mockSubscription.update(any(SubscriptionUpdateParams.class), any(RequestOptions.class))).thenReturn(mockSubscription);

      subscriptionService.cancel(props);

      ArgumentCaptor<SubscriptionUpdateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionUpdateParams.class);
      verify(mockSubscription).update(paramsCaptor.capture(), any(RequestOptions.class));

      assertThat(paramsCaptor.getValue().getCancelAtPeriodEnd()).isTrue();
    }
  }

  @Test
  @DisplayName("cancel - propagates StripeException")
  void cancel_propagatesStripeException() throws StripeException {
    try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class)) {

      Map<String, String> props = Map.of(
        "stripeAccountId", "acct_test",
        "subscriptionId", "sub_test"
      );

      CardException cardException = mock(CardException.class);
      subscriptionMock.when(() -> Subscription.retrieve(anyString(), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> subscriptionService.cancel(props))
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

      subscriptionService.createTaxRate();

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

      assertThatThrownBy(() -> subscriptionService.createTaxRate())
        .isInstanceOf(StripeException.class);
    }
  }
}
