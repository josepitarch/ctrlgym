package dev.jpitarch.ctrlgym.core.services;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.domain.exceptions.DuplicateMembershipException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MembershipNotFoundException;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.core.repositories.StripeBridge;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipsRepository membershipsRepository;

  private final SubscriptionService subscriptionService;

  private final StripeBridge stripeBridge;

  public void initialize(Member.Id memberId, String membershipPlanId) throws StripeException {
    if (membershipsRepository.hasActiveMembership(memberId, membershipPlanId)) {
      throw new DuplicateMembershipException(memberId, membershipPlanId);
    }

    String stripeAccountId = stripeBridge.getStripeAccountId(memberId.gymId());
    String stripePriceId = stripeBridge.getStripePriceId(membershipPlanId);
    Optional<String> paymentMethodId = stripeBridge.getPaymentMethodId(memberId);
    Optional<String> customerId = stripeBridge.getStripeCustomerId(memberId);

    if (paymentMethodId.isEmpty() || customerId.isEmpty()) {
      throw new IllegalStateException("Customer or payment method not found for member with id " + memberId);
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
    var currentMembership = membershipsRepository.getMemberships(memberId).stream().filter(m -> m.getDatePeriod().isCurrent()).findFirst();
    if (currentMembership.isEmpty()) throw new MembershipNotFoundException(memberId);
    String stripeSubscriptionId = stripeBridge.getStripeSubscriptionId(memberId, currentMembership.get().getId());
    String stripeAccountId = stripeBridge.getStripeAccountId(memberId.gymId());
    String currentStripePriceId = stripeBridge.getStripePriceId(stripeSubscriptionId);
    String newCurrentStripePriceId = stripeBridge.getStripePriceId(newMembershipPlanId);
    subscriptionService.change(stripeSubscriptionId, currentStripePriceId, newCurrentStripePriceId, stripeAccountId);
  }

  public void cancel(Member.Id memberId, Integer membershipId, Integer cancellationReasonId, String comment) throws StripeException {
    var props = Map.of(
      "stripeAccountId", stripeBridge.getStripeAccountId(memberId.gymId()),
      "subscriptionId", stripeBridge.getStripeSubscriptionId(memberId, membershipId)
    );

    log.info("Cancelling membership plan with id {} for member with id {}...", membershipId, memberId);

    subscriptionService.cancel(props);
    membershipsRepository.setCancellationReasonId(membershipId, cancellationReasonId, comment);
  }

  public Optional<Membership> retrieve(Member.Id memberId) {
    log.debug("Retrieving memberships for member with id {}...", memberId);
    var memberships = membershipsRepository.getMemberships(memberId);

    return memberships.stream()
      .filter(m -> m.getDatePeriod().isCurrent())
      .findFirst()
      .or(() -> memberships.stream()
        .filter(m -> m.getDatePeriod().isPast())
        .max(Comparator.comparing(m -> m.getDatePeriod().to()))
      );
  }

  public List<MembershipCancellationReason> getCancellationReasons() {
    var language = LocaleContextHolder.getLocale().getLanguage();
    return membershipsRepository.getCancellationReasons(language);
  }

  //TODO: revisar si esto ya viene pre-informado con el evento Invoice
  private LocalDate calculateNextBillingDate() {
    var today = LocalDate.now();
    if (today.getDayOfMonth() == 1) {
      return today.plusMonths(1).withDayOfMonth(1);
    }
    return today.plusMonths(2).withDayOfMonth(1);
  }

}
