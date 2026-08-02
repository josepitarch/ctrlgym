package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.StripeBridge;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
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
class InvoicesServiceTest {

  @InjectMocks
  InvoicesService invoicesService;

  @Mock
  StripeBridge stripeBridge;

  private final Member.Id memberId = new Member.Id(UUID.randomUUID(), 1);

  @Test
  @DisplayName("createSetupIntent - creates setup intent with correct parameters")
  void createSetupIntent_createsSetupIntentWithCorrectParameters() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      String accountId = "acct_test123";
      String customerId = "cus_test123";

      when(stripeBridge.getStripeAccountId(memberId.gymId())).thenReturn(accountId);
      when(stripeBridge.getStripeCustomerId(memberId)).thenReturn(Optional.of(customerId));

      SetupIntent mockSetupIntent = mock(SetupIntent.class);
      when(mockSetupIntent.getId()).thenReturn("seti_test123");
      when(mockSetupIntent.getClientSecret()).thenReturn("seti_test123_secret_abc");

      setupIntentMock.when(() -> SetupIntent.create(any(SetupIntentCreateParams.class), any(RequestOptions.class)))
        .thenReturn(mockSetupIntent);

      SetupIntentResponse result = invoicesService.createSetupIntent(memberId);

      assertThat(result.id()).isEqualTo("seti_test123");
      assertThat(result.clientSecret()).isEqualTo("seti_test123_secret_abc");

      ArgumentCaptor<SetupIntentCreateParams> paramsCaptor = ArgumentCaptor.forClass(SetupIntentCreateParams.class);
      setupIntentMock.verify(() -> SetupIntent.create(paramsCaptor.capture(), any(RequestOptions.class)));

      SetupIntentCreateParams capturedParams = paramsCaptor.getValue();
      assertThat(capturedParams.getCustomer()).isEqualTo(customerId);
      assertThat(capturedParams.getPaymentMethodTypes()).containsExactly("sepa_debit");
      assertThat(capturedParams.getUsage()).isEqualTo(SetupIntentCreateParams.Usage.OFF_SESSION);
    }
  }

  @Test
  @DisplayName("createSetupIntent - throws NoSuchElementException when customer not found")
  void createSetupIntent_throwsExceptionWhenCustomerNotFound() {
    when(stripeBridge.getStripeAccountId(memberId.gymId())).thenReturn("acct_test");
    when(stripeBridge.getStripeCustomerId(memberId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> invoicesService.createSetupIntent(memberId))
      .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("createSetupIntent - propagates StripeException")
  void createSetupIntent_propagatesStripeException() throws StripeException {
    try (MockedStatic<SetupIntent> setupIntentMock = mockStatic(SetupIntent.class)) {

      String accountId = "acct_test";
      String customerId = "cus_test";

      when(stripeBridge.getStripeAccountId(memberId.gymId())).thenReturn(accountId);
      when(stripeBridge.getStripeCustomerId(memberId)).thenReturn(Optional.of(customerId));

      CardException cardException = mock(CardException.class);
      setupIntentMock.when(() -> SetupIntent.create(any(SetupIntentCreateParams.class), any(RequestOptions.class)))
        .thenThrow(cardException);

      assertThatThrownBy(() -> invoicesService.createSetupIntent(memberId))
        .isInstanceOf(StripeException.class);
    }
  }
}
