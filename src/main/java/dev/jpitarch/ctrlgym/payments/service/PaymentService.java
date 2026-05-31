package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.payments.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final StripeService stripeService;

  public ConnectAccountResponse createGymAccount(ConnectAccountRequest request) {
    try {
      return stripeService.createConnectAccount(request);
    } catch (StripeException e) {
      log.error("Failed to create Connect account for gym: {}", request.getBusinessName(), e);
      throw new RuntimeException("Failed to create gym account: " + e.getMessage(), e);
    }
  }

  public PaymentResponse createMembershipPayment(PaymentIntentRequest request) {
    try {
      return stripeService.createPaymentIntent(request);
    } catch (StripeException e) {
      log.error("Failed to create payment intent for membership: {}", request.getMembershipId(), e);
      throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
    }
  }

  public PaymentResponse getPaymentStatus(String paymentIntentId) {
    try {
      return stripeService.retrievePaymentIntent(paymentIntentId);
    } catch (StripeException e) {
      log.error("Failed to retrieve payment intent: {}", paymentIntentId, e);
      throw new RuntimeException("Failed to get payment status: " + e.getMessage(), e);
    }
  }

  public String getOnboardingLink(String accountId, String refreshUrl, String returnUrl) {
    try {
      return stripeService.createAccountSession(accountId, refreshUrl, returnUrl);
    } catch (StripeException e) {
      log.error("Failed to create onboarding link for account: {}", accountId, e);
      throw new RuntimeException("Failed to create onboarding link: " + e.getMessage(), e);
    }
  }

  public boolean isAccountActive(String accountId) {
    try {
      return stripeService.isAccountActive(accountId);
    } catch (StripeException e) {
      log.error("Failed to check account status for: {}", accountId, e);
      throw new RuntimeException("Failed to check account status: " + e.getMessage(), e);
    }
  }
}
