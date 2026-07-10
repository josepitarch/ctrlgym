package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.CreateMembershipPlanRequest;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.core.services.ExercisesService;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymUseCase {

  private final GymsRepository gymsRepository;

  private final ExercisesService exercisesService;

  private final MembershipsRepository membershipsRepository;

  private final ProductService productService;

  public List<GymBranch> getBranches(Integer gymId) {
    return gymsRepository.getBranches(gymId);
  }

  public List<Member> getMembers(GymBranchId gymBranchId) {
    return gymsRepository.getMembers(gymBranchId);
  }

  public MemberRetention getMemberRetention(GymBranchId gymBranchId, Member.Id memberId) {
    return new MemberRetention(memberId, 85, 2340, 14, 9);
  }

  public void createMembershipPlan(Integer gymId, CreateMembershipPlanRequest request) throws StripeException {
    MembershipPlan membershipPlan = productService.create(gymId, request);
    membershipsRepository.createMembershipPlan(membershipPlan, gymId);
  }

  public List<MembershipPlan> getMembershipPlans(Integer gymId) {
    return membershipsRepository.getMembershipPlans(gymId);
  }

  public void deleteMembershipPlan(String planId, Integer gymId) throws StripeException {
    productService.delete(gymId, planId);
    membershipsRepository.deleteMembershipPlan(planId, gymId);
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

}
