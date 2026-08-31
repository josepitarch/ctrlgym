package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import dev.jpitarch.ctrlgym.payments.dtos.SetupIntentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @InjectMocks
  CustomerService customerService;

  @Mock
  StripeBridge stripeBridge;

  private final Member.Id memberId = new Member.Id(UUID.randomUUID());
  private final Integer gymId = 1;

  @Test
  @DisplayName("create - creates customer with correct parameters")
  void create_createsCustomerWithCorrectParameters() throws StripeException {
    try (MockedStatic<Customer> customerMock = mockStatic(Customer.class)) {

      String stripeAccountId = "acct_test123";
      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(stripeAccountId);

      Member member = Member.builder()
        .id(memberId)
        .gymId(gymId)
        .name("John")
        .firstSurname("Doe")
        .secondSurname("Smith")
        .email("john.doe@example.com")
        .nif("12345678A")
        .address(Member.Address.builder()
          .city("Madrid")
          .postalCode(28001)
          .build())
        .build();

      Customer mockCustomer = mock(Customer.class);
      when(mockCustomer.getId()).thenReturn("cus_test123");

      customerMock.when(() -> Customer.create(any(CustomerCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockCustomer);

      String result = customerService.create(member);

      assertThat(result).isEqualTo("cus_test123");

      ArgumentCaptor<CustomerCreateParams> paramsCaptor = ArgumentCaptor.forClass(CustomerCreateParams.class);
      customerMock.verify(() -> Customer.create(paramsCaptor.capture(), any(RequestOptions.class)));

      CustomerCreateParams capturedParams = paramsCaptor.getValue();
      assertThat(capturedParams.getName()).isEqualTo("John Doe Smith");
      assertThat(capturedParams.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(capturedParams.getMetadata()).extracting("nif").isEqualTo("12345678A");
      assertThat(capturedParams.getMetadata()).extracting("gym_id").isEqualTo("1");
    }
  }

  @Test
  @DisplayName("create - propagates StripeException")
  void create_propagatesStripeException() throws StripeException {
    try (MockedStatic<Customer> customerMock = mockStatic(Customer.class)) {

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");

      Member member = Member.builder()
        .id(memberId)
        .gymId(gymId)
        .name("John")
        .firstSurname("Doe")
        .email("john@example.com")
        .nif("12345678A")
        .address(Member.Address.builder()
          .city("Madrid")
          .postalCode(28001)
          .build())
        .build();

      CardException cardException = mock(CardException.class);
      customerMock.when(() -> Customer.create(any(CustomerCreateParams.class), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> customerService.create(member))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("createSetupIntent - creates setup intent with correct parameters")
  void createSetupIntent_createsSetupIntentWithCorrectParameters() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      String accountId = "acct_test123";
      String customerId = "cus_test123";

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(accountId);
      when(stripeBridge.getStripeCustomerId(memberId.memberId())).thenReturn(Optional.of(customerId));

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getId()).thenReturn("seti_test123");
      when(mockSetupIntent.getClientSecret()).thenReturn("seti_test123_secret_abc");

      setupIntentMock.when(() -> SetupIntent.create(any(SetupIntentCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      SetupIntentResponse result = customerService.createSetupIntent(memberId.memberId(), gymId);

      assertThat(result.id()).isEqualTo("seti_test123");
      assertThat(result.clientSecret()).isEqualTo("seti_test123_secret_abc");

      ArgumentCaptor<SetupIntentCreateParams> paramsCaptor = ArgumentCaptor.forClass(SetupIntentCreateParams.class);
      setupIntentMock.verify(() -> SetupIntent.create(paramsCaptor.capture(), any(RequestOptions.class)));

      SetupIntentCreateParams capturedParams = paramsCaptor.getValue();
      assertThat(capturedParams.getCustomer()).isEqualTo(customerId);
      assertThat(capturedParams.getPaymentMethodTypes()).containsExactly("sepa_debit");
      assertThat(capturedParams.getUsage()).isEqualTo(SetupIntentCreateParams.Usage.OFF_SESSION);

      verify(stripeBridge).saveStripeSetupIntentId(memberId.memberId(),"seti_test123");
    }
  }

  @Test
  @DisplayName("createSetupIntent - throws NoSuchElementException when customer not found")
  void createSetupIntent_throwsExceptionWhenCustomerNotFound() {
    when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
    when(stripeBridge.getStripeCustomerId(memberId.memberId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.createSetupIntent(memberId.memberId(), gymId))
      .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("createSetupIntent - propagates StripeException")
  void createSetupIntent_propagatesStripeException() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      String accountId = "acct_test";
      String customerId = "cus_test";

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn(accountId);
      when(stripeBridge.getStripeCustomerId(memberId.memberId())).thenReturn(Optional.of(customerId));

      CardException cardException = mock(CardException.class);
      setupIntentMock.when(() -> SetupIntent.create(any(SetupIntentCreateParams.class), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> customerService.createSetupIntent(memberId.memberId(), gymId))
        .isInstanceOf(StripeException.class);
    }
  }

  @Test
  @DisplayName("updateSetupIntentId - updates subscription and detaches old payment method")
  void updateSetupIntentId_updatesSubscriptionAndDetachesOldPaymentMethod() throws StripeException {
    try (MockedStatic<Subscription> subscriptionMock = mockStatic(Subscription.class);
         MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class);
         MockedStatic<PaymentMethod> paymentMethodMock = mockStatic(PaymentMethod.class)) {

      String subscriptionId = "sub_test123";
      String oldSetupIntentId = "seti_old";
      String oldPaymentMethodId = "pm_old";
      String newPaymentMethodId = "pm_new";
      String stripeAccount = "acct_test";

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getPaymentMethod()).thenReturn(oldPaymentMethodId);

      setupIntentMock.when(() -> SetupIntent.retrieve(eq(oldSetupIntentId), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      Subscription mockSubscription = mock(Subscription.class);
      PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);

      subscriptionMock.when(() -> Subscription.retrieve(eq(subscriptionId), any(RequestOptions.class)))
        .thenReturn(mockSubscription);
      when(mockSubscription.update(any(SubscriptionUpdateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSubscription);

      paymentMethodMock.when(() -> PaymentMethod.retrieve(eq(oldPaymentMethodId), any(RequestOptions.class)))
        .thenReturn(mockPaymentMethod);
      when(mockPaymentMethod.detach(any(RequestOptions.class))).thenReturn(mockPaymentMethod);

      customerService.updateSetupIntentId(subscriptionId, oldSetupIntentId, newPaymentMethodId, stripeAccount);

      ArgumentCaptor<SubscriptionUpdateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionUpdateParams.class);
      verify(mockSubscription).update(paramsCaptor.capture(), any(RequestOptions.class));
      assertThat(paramsCaptor.getValue().getDefaultPaymentMethod()).isEqualTo(newPaymentMethodId);

      paymentMethodMock.verify(() -> PaymentMethod.retrieve(eq(oldPaymentMethodId), any(RequestOptions.class)));
      verify(mockPaymentMethod).detach(any(RequestOptions.class));
    }
  }

  @Test
  @DisplayName("updateSetupIntentId - skips subscription update when subscriptionId is null")
  void updateSetupIntentId_skipsSubscriptionUpdateWhenSubscriptionIdIsNull() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class);
         MockedStatic<PaymentMethod> paymentMethodMock = mockStatic(PaymentMethod.class)) {

      String oldSetupIntentId = "seti_old";
      String oldPaymentMethodId = "pm_old";
      String newPaymentMethodId = "pm_new";
      String stripeAccount = "acct_test";

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getPaymentMethod()).thenReturn(oldPaymentMethodId);

      setupIntentMock.when(() -> SetupIntent.retrieve(eq(oldSetupIntentId), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);

      paymentMethodMock.when(() -> PaymentMethod.retrieve(eq(oldPaymentMethodId), any(RequestOptions.class)))
        .thenReturn(mockPaymentMethod);
      when(mockPaymentMethod.detach(any(RequestOptions.class))).thenReturn(mockPaymentMethod);

      customerService.updateSetupIntentId(null, oldSetupIntentId, newPaymentMethodId, stripeAccount);

      paymentMethodMock.verify(() -> PaymentMethod.retrieve(eq(oldPaymentMethodId), any(RequestOptions.class)));
      verify(mockPaymentMethod).detach(any(RequestOptions.class));
    }
  }

  @Test
  @DisplayName("updateSetupIntentId - propagates StripeException")
  void updateSetupIntentId_propagatesStripeException() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      CardException cardException = mock(CardException.class);
      setupIntentMock.when(() -> SetupIntent.retrieve(anyString(), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> customerService.updateSetupIntentId("sub_test", "seti_old", "pm_new", "acct_test"))
        .isInstanceOf(StripeException.class);
    }
  }
}
