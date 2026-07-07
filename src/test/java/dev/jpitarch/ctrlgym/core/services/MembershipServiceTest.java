package dev.jpitarch.ctrlgym.core.services;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

  @InjectMocks
  MembershipService membershipService;

  @Mock
  MembershipsRepository membershipsRepository;

  @Mock
  GymsRepository gymsRepository;

  @Mock
  MembersRepository membersRepository;

  @Mock
  SubscriptionService subscriptionService;

  private final Member.Id memberId = new Member.Id(java.util.UUID.randomUUID(), 1);

  @ParameterizedTest
  @CsvSource({
          "2026-07-01, 2026-08-01",
          "2026-07-02, 2026-09-01",
          "2026-07-15, 2026-09-01",
          "2026-07-31, 2026-09-01",
          "2026-08-01, 2026-09-01",
          "2026-08-15, 2026-10-01",
          "2026-12-31, 2027-02-01",
          "2026-01-01, 2026-02-01"
  })
  void initialize_calculatesCorrectNextBillingDate(String today, String expectedNextBilling) throws StripeException {
    var ld = LocalDate.parse(today);
    var expected = LocalDate.parse(expectedNextBilling);
    try (var mockedDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
      mockedDate.when(LocalDate::now).thenReturn(ld);

      when(gymsRepository.getStripeAccountId(1)).thenReturn("stripe_account");
      when(membershipsRepository.getStripePriceId("plan_basic")).thenReturn("price_basic");
      when(membersRepository.getPaymentMethodId(memberId)).thenReturn(Optional.of("pm_test"));
      when(membersRepository.getStripeCustomerId(memberId)).thenReturn(Optional.of("cus_test"));
      when(subscriptionService.create(any(), any())).thenReturn("sub_test123");

      membershipService.initialize(memberId, "plan_basic");

      ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
      verify(membershipsRepository).save(eq(memberId), eq("plan_basic"), eq("sub_test123"), dateCaptor.capture());

      assertThat(dateCaptor.getValue()).isEqualTo(expected);
    }
  }
}
