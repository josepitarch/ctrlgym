package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.payments.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

  public ConnectAccountResponse createConnectAccount(ConnectAccountRequest request) throws StripeException {
    var params = AccountCreateParams.builder()
      .setType(AccountCreateParams.Type.EXPRESS)
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
          .setSepaDebitPayments(
            AccountCreateParams.Capabilities.SepaDebitPayments.builder()
              .setRequested(true)
              .build()
          )
          .build())
      .build();

    var account = Account.create(params);

    var linkParams = AccountLinkCreateParams.builder()
      .setAccount(account.getId())
      .setRefreshUrl("https://app.ctrlgym.es/reauthenticate")
      .setReturnUrl("https://app.ctrlgym.es/onboarding-complete")
      .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
      .build();

    var accountLink = AccountLink.create(linkParams);

    return ConnectAccountResponse.builder()
      .accountId(account.getId())
      .onboardingUrl(accountLink.getUrl())
      .build();
  }

  public boolean isAccountActive(String accountId) throws StripeException {
    var account = Account.retrieve(accountId);
    return account.getChargesEnabled() && account.getPayoutsEnabled();
  }

}
