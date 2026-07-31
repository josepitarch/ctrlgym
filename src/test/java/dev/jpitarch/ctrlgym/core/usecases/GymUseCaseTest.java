package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.exceptions.CoreBusinessException;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.services.ExercisesService;
import dev.jpitarch.ctrlgym.core.services.GenerateInvoiceReportService;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymUseCaseTest {

  @InjectMocks
  GymUseCase gymUseCase;

  @Mock
  MembershipPlanRepository membershipPlanRepository;

  @Mock
  ProductService productService;

  @Test
  @DisplayName("Should throw CoreBusinessException when gymBranchId is null and allBranches is false")
  void createMembershipPlan_nullBranchIdAndNotAllBranches_throwsException() {
    MembershipPlan plan = MembershipPlan.builder()
        .name("Basic Plan")
        .price(29.99)
        .gymBranchId(null)
        .allBranches(false)
        .build();

    assertThatThrownBy(() -> gymUseCase.createMembershipPlan(1, plan))
        .isInstanceOf(CoreBusinessException.class)
        .hasMessageContaining("gymBranchId is informed and allBranches is true or vice versa");

    verifyNoInteractions(productService);
    verifyNoInteractions(membershipPlanRepository);
  }

  @Test
  @DisplayName("Should throw CoreBusinessException when gymBranchId is set and allBranches is true")
  void createMembershipPlan_branchIdSetAndAllBranches_throwsException() {
    MembershipPlan plan = MembershipPlan.builder()
        .name("Basic Plan")
        .price(29.99)
        .gymBranchId(5)
        .allBranches(true)
        .build();

    assertThatThrownBy(() -> gymUseCase.createMembershipPlan(1, plan))
        .isInstanceOf(CoreBusinessException.class)
        .hasMessageContaining("gymBranchId is informed and allBranches is true or vice versa");

    verifyNoInteractions(productService);
    verifyNoInteractions(membershipPlanRepository);
  }

  @Test
  @DisplayName("Should create membership plan successfully when validation passes")
  void createMembershipPlan_validPlan_createsSuccessfully() throws StripeException {
    MembershipPlan plan = MembershipPlan.builder()
        .name("Basic Plan")
        .price(29.99)
        .gymBranchId(5)
        .allBranches(false)
        .build();

    String[] stripeData = {"prod_abc123", "price_xyz789"};
    when(productService.create(eq(1), any(MembershipPlan.class))).thenReturn(stripeData);

    gymUseCase.createMembershipPlan(1, plan);

    assertThat(plan.getId()).isEqualTo("prod_abc123");

    ArgumentCaptor<MembershipPlan> planCaptor = ArgumentCaptor.forClass(MembershipPlan.class);
    verify(productService).create(eq(1), planCaptor.capture());
    assertThat(planCaptor.getValue()).isEqualTo(plan);

    verify(membershipPlanRepository).create(plan, 1, "price_xyz789");
  }
}
