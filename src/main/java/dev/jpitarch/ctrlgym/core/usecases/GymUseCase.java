package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.exceptions.CoreBusinessException;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.core.services.ExercisesService;
import dev.jpitarch.ctrlgym.core.services.GenerateInvoiceReportService;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymUseCase {

  private final GymsRepository gymsRepository;

  private final ExercisesService exercisesService;

  private final MembershipPlanRepository membershipPlanRepository;

  private final InvoiceRepository invoiceRepository;

  private final ProductService productService;

  private final GenerateInvoiceReportService generateInvoiceReportService;

  public List<GymBranch> getBranches(Integer gymId) {
    return gymsRepository.getBranches(gymId);
  }

  public List<Member> getMembers(GymBranchId gymBranchId) {
    return gymsRepository.getMembers(gymBranchId);
  }

  public MemberRetention getMemberRetention(GymBranchId gymBranchId, Member.Id memberId) {
    return new MemberRetention(memberId, 85, 2340, 14, 9);
  }

  public Page<Invoice> getInvoices(GymBranchId gymBranchId, Member.Id memberId, Pageable pageable) {
    return invoiceRepository.findByMemberId(memberId, pageable);
  }


  public void createMembershipPlan(Integer gymId, MembershipPlan plan) throws StripeException {
    if ((plan.getGymBranchId() == null && !plan.isAllBranches()) || (plan.getGymBranchId() != null && plan.isAllBranches())) {
      throw new CoreBusinessException(MembershipPlan.class, "gymBranchId is informed and allBranches is true or vice versa");
    }
    String[] data = productService.create(gymId, plan);
    plan.setId(data[0]);
    membershipPlanRepository.create(plan, gymId, data[1]);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    log.debug("Retrieving membership plans for gym with id {}...", gymBranchId);
    return membershipPlanRepository.getMembershipPlans(gymBranchId);
  }

  public void deleteMembershipPlan(String planId, Integer gymId) throws StripeException {
    productService.delete(gymId, planId);
    membershipPlanRepository.delete(planId, gymId);
  }

  public CurrentOccupancy getCurrentOccupancy(GymBranchId gymBranchId) {
    GymBranch gymBranch = gymsRepository.getGymBranch(gymBranchId);
    return new CurrentOccupancy(gymsRepository.getCurrentOccupancy(gymBranchId), gymBranch.getCapacity(), gymBranch.getPeakHour());
  }

  public List<Exercise> getAll(Integer gymId) {
    return exercisesService.getAll(gymId);
  }

  public Exercise createExercise(Integer gymId, Exercise exercise) {
    return exercisesService.create(exercise, gymId);
  }

  public void deleteExercise(Integer exerciseId, Integer gymId) {
    exercisesService.delete(exerciseId, gymId);
  }

  public byte[] getMemberInvoiceReport(GymBranchId gymBranchId, Member.Id memberId, String invoiceId) throws IOException {
    log.info("Generating invoice report for member {} and invoice {}...", memberId, invoiceId);
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }

}
