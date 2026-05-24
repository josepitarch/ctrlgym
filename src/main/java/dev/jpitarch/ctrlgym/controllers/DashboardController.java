package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.domain.*;
import dev.jpitarch.ctrlgym.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.domain.enums.MemberDistribution;
import dev.jpitarch.ctrlgym.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.usecases.DashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class DashboardController {

  private final DashboardUseCase useCase;

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/occupancy")
  public List<Map<String, Integer>> getOccupancies(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam Granularity granularity) {
    return useCase.getOccupancies(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), granularity);
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships")
  public List<Map<YearMonth, Integer>> getMemberships(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam MembershipFlow flow) {
    return useCase.getMemberships(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), flow);
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships/seniority-avg")
  public Integer getMembershipSeniorityAverage(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return useCase.getMembershipSeniorityAverage(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to));
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships/cohorts")
  public List<Cohort> getCohorts(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getCohorts(GymBranchId.of(gymId, branchId));
  }


  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships/cancellation-reasons")
  public List<Map<String, Integer>> getCancellationReasons(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getCancellationReasons(GymBranchId.of(gymId, branchId));
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/expenses")
  public List<Expense> getExpenses(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getExpenses(GymBranchId.of(gymId, branchId));
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/cash-flow")
  public Map<String, List<Map<YearMonth, Double>>> getCashFlow(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return useCase.getCashFlow(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to));
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/members")
  public List<Member> getMembers(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getMembers(GymBranchId.of(gymId, branchId));
  }


  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/members/distribution")
  public Map<MemberDistribution, List<String[]>> getMembersDistribution(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getMembersDistribution(GymBranchId.of(gymId, branchId));
  }


}
