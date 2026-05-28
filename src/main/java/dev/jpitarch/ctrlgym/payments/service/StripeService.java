package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import dev.jpitarch.ctrlgym.payments.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class StripeService {

  public ConnectAccountResponse createConnectAccount(ConnectAccountRequest request) throws StripeException {
    AccountCreateParams params = AccountCreateParams.builder()
      .setType(AccountCreateParams.Type.STANDARD)
      .setBusinessType(AccountCreateParams.BusinessType.COMPANY)
      .setCompany(AccountCreateParams.Company.builder()
        .setName(request.getBusinessName())
        .build())
      .setEmail(request.getEmail())
      .setCountry(request.getCountry())
      .setCapabilities(
        AccountCreateParams.Capabilities.builder()
          .setCardPayments(
            AccountCreateParams.Capabilities.CardPayments.builder()
              .setRequested(true)
              .build()
          )
          .setTransfers(
            AccountCreateParams.Capabilities.Transfers.builder()
              .setRequested(true)
              .build()
          )
          .build())
      .build();

    Account account = Account.create(params);

    AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
      .setAccount(account.getId())
      .setRefreshUrl("https://app.ctrlgym.es/reauthenticate")
      .setReturnUrl("https://app.ctrlgym.es/onboarding-complete")
      .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
      .build();

    AccountLink accountLink = AccountLink.create(linkParams);

    return ConnectAccountResponse.builder()
      .accountId(account.getId())
      .onboardingUrl(accountLink.getUrl())
      .build();
  }

  public PaymentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
    PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
      .setAmount(request.getAmount())
      .setCurrency(request.getCurrency() != null ? request.getCurrency() : "eur")
      .addPaymentMethodType("card")
      .setDescription(request.getDescription())
      .setTransferData(PaymentIntentCreateParams.TransferData.builder()
        .setDestination(request.getAccountId())
        .build());

    PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

    return PaymentResponse.builder()
      .id(paymentIntent.getId())
      .stripePaymentIntentId(paymentIntent.getId())
      .amount(paymentIntent.getAmount())
      .currency(paymentIntent.getCurrency())
      .status(paymentIntent.getStatus())
      .clientSecret(paymentIntent.getClientSecret())
      .createdAt(LocalDateTime.now())
      .build();
  }

  public PaymentResponse retrievePaymentIntent(String paymentIntentId) throws StripeException {
    PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

    return PaymentResponse.builder()
      .id(paymentIntent.getId())
      .stripePaymentIntentId(paymentIntent.getId())
      .amount(paymentIntent.getAmount())
      .currency(paymentIntent.getCurrency())
      .status(paymentIntent.getStatus())
      .createdAt(LocalDateTime.now())
      .build();
  }

  public String createCustomer(String email, String name) throws StripeException {
    CustomerCreateParams params = CustomerCreateParams.builder()
      .setEmail(email)
      .setName(name)
      .build();

    Customer customer = Customer.create(params);
    return customer.getId();
  }

  public boolean isAccountActive(String accountId) throws StripeException {
    Account account = Account.retrieve(accountId);
    return account.getChargesEnabled() && account.getPayoutsEnabled();
  }

  public String createAccountSession(String accountId, String refreshUrl, String returnUrl) throws StripeException {
    AccountLinkCreateParams params = AccountLinkCreateParams.builder()
      .setAccount(accountId)
      .setRefreshUrl(refreshUrl)
      .setReturnUrl(returnUrl)
      .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
      .build();

    AccountLink accountLink = AccountLink.create(params);
    return accountLink.getUrl();
  }
}
