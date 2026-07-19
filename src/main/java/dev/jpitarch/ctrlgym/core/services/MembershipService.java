package dev.jpitarch.ctrlgym.core.services;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.domain.exceptions.DuplicateMembershipException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MembershipNotFoundException;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipsRepository membershipsRepository;

  private final MembershipPlanRepository membershipPlanRepository;

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final SubscriptionService subscriptionService;

  public void initialize(Member.Id memberId, String membershipPlanId) throws StripeException {
    if (membershipsRepository.hasActiveMembership(memberId, membershipPlanId)) {
      throw new DuplicateMembershipException(memberId, membershipPlanId);
    }

    String stripeAccountId = gymsRepository.getStripeAccountId(memberId.gymId());
    String stripePriceId = membershipPlanRepository.getStripePriceId(membershipPlanId);
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

    log.info("Initializing membership plan with id {} for member with id {}...", membershipPlanId, memberId);

    String subscriptionId = subscriptionService.create(memberId, props);
    membershipsRepository.save(memberId, membershipPlanId, subscriptionId, calculateNextBillingDate());
  }

  public void change(Member.Id memberId, String newMembershipPlanId) throws StripeException {
    Optional<Membership> currentMembership = membershipsRepository.getMembership(memberId);
    if (currentMembership.isEmpty()) throw new MembershipNotFoundException(memberId);
    String stripeSubscriptionId = membershipPlanRepository.getStripeSubscriptionId(memberId, currentMembership.get().getId());
    String currentStripePriceId = membershipPlanRepository.getStripePriceId(stripeSubscriptionId);
    String newCurrentStripePriceId = membershipPlanRepository.getStripePriceId(newMembershipPlanId);
    subscriptionService.change(stripeSubscriptionId, currentStripePriceId, newCurrentStripePriceId);
  }

  public void cancel(Member.Id memberId, Integer membershipId, Integer cancellationReasonId, String comment) throws StripeException {
    var props = Map.of(
            "stripeAccountId", gymsRepository.getStripeAccountId(memberId.gymId()),
            "subscriptionId", membershipPlanRepository.getStripeSubscriptionId(memberId, membershipId)
    );

    log.info("Cancelling membership plan with id {} for member with id {}...", membershipId, memberId);

    subscriptionService.cancel(props);
    membershipsRepository.setCancellationReasonId(membershipId, cancellationReasonId, comment);
  }

  public Membership retrieve(Member.Id memberId) {
    log.debug("Retrieving memberships for member with id {}...", memberId);
    return membershipsRepository.getMembership(memberId).orElse(null);
  }

  public List<MembershipCancellationReason> getCancellationReasons() {
    var language = LocaleContextHolder.getLocale().getLanguage();
    return membershipsRepository.getCancellationReasons(language);
  }

  private LocalDate calculateNextBillingDate() {
    var today = LocalDate.now();
    if (today.getDayOfMonth() == 1) {
      return today.plusMonths(1).withDayOfMonth(1);
    }
    return today.plusMonths(2).withDayOfMonth(1);
  }

}
