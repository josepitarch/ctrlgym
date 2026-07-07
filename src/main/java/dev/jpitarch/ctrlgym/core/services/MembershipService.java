package dev.jpitarch.ctrlgym.core.services;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipsRepository membershipsRepository;

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final SubscriptionService subscriptionService;

  public void initialize(Member.Id memberId, String membershipId) throws StripeException {
    if (membershipsRepository.hasActiveMembership(memberId, membershipId)) {
      throw new IllegalStateException("Member " + memberId + " already has membership " + membershipId);
    }

    String stripeAccountId = gymsRepository.getStripeAccountId(memberId.gymId());
    String stripePriceId = membershipsRepository.getStripePriceId(membershipId);
    Optional<String> paymentMethodId = membersRepository.getPaymentMethodId(memberId);
    Optional<String> customerId = membersRepository.getStripeCustomerId(memberId);

    if (paymentMethodId.isEmpty() || customerId.isEmpty()) {
      throw new IllegalStateException("Customer or payment method not found for member " + memberId);
    }

    var props = Map.of(
      "stripeAccountId", stripeAccountId,
      "stripePriceId", stripePriceId,
      "paymentMethodId", paymentMethodId.get(),
      "customerId", customerId.get()
    );

    String subscriptionId = subscriptionService.createSubscription(memberId, props);
    membershipsRepository.save(memberId, membershipId, subscriptionId);
  }

  public void cancel(Member.Id memberId, String membershipId, Integer cancellationReasonId) throws StripeException {
    if (!membershipsRepository.hasActiveMembership(memberId, membershipId)) {
      throw new IllegalStateException("Membership " + membershipId + " not found for member " + membershipId);
    }

    String stripeAccountId = gymsRepository.getStripeAccountId(memberId.gymId());
    String subscriptionId = membershipsRepository.getStripeSubscriptionId(memberId, membershipId);

    var props = Map.of(
      "stripeAccountId", stripeAccountId,
      "subscriptionId", subscriptionId
    );

    subscriptionService.cancelSubscription(props);
    membershipsRepository.setCancellationReasonId(memberId, membershipId, cancellationReasonId);
  }

  public List<Membership> getMemberships(Member.Id memberId) {
    log.info("Getting memberships for member {}...", memberId);
    return membershipsRepository.getMemberships(memberId);
  }

  public List<MembershipCancellationReason> getCancellationReasons() {
    var language = LocaleContextHolder.getLocale().getLanguage();
    return membershipsRepository.getCancellationReasons(language);
  }

}
