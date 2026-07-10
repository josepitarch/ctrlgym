package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberDistribution;
import dev.jpitarch.ctrlgym.core.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.core.usecases.DashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dashboard")
public class DashboardController {

  private final DashboardUseCase useCase;

  @GetMapping("/gyms/{gymId}/branches/{branchId}/occupancy")
  public ResponseEntity<List<Map<String, Integer>>> getOccupancies(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam Granularity granularity) {
    return ResponseEntity.ok(useCase.getOccupancies(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), granularity));
  }

  @GetMapping("/gyms/{gymId}/branches/{branchId}/memberships")
  public ResponseEntity<List<Map<YearMonth, Integer>>> getMemberships(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam MembershipFlow flow) {
    return ResponseEntity.ok(useCase.getMemberships(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), flow));
  }

  @GetMapping("/gyms/{gymId}/branches/{branchId}/memberships/seniority-avg")
  public ResponseEntity<Integer> getMembershipSeniorityAverage(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return ResponseEntity.ok(useCase.getMembershipSeniorityAverage(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to)));
  }

  @GetMapping("/gyms/{gymId}/branches/{branchId}/memberships/cohorts")
  public ResponseEntity<List<Cohort>> getCohorts(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getCohorts(GymBranchId.of(gymId, branchId)));
  }


  @GetMapping("/gyms/{gymId}/branches/{branchId}/memberships/cancellation-reasons")
  public ResponseEntity<List<Map<String, Integer>>> getCancellationReasons(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getCancellationReasons(GymBranchId.of(gymId, branchId)));
  }

  @GetMapping("/gyms/{gymId}/branches/{branchId}/expenses")
  public ResponseEntity<List<Expense>> getExpenses(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getExpenses(GymBranchId.of(gymId, branchId)));
  }

  @GetMapping("/gyms/{gymId}/branches/{branchId}/cash-flow")
  public ResponseEntity<Map<String, List<Map<YearMonth, Double>>>> getCashFlow(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return ResponseEntity.ok(useCase.getCashFlow(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to)));
  }


  @GetMapping("/gyms/{gymId}/branches/{branchId}/members/distribution")
  public ResponseEntity<Map<MemberDistribution, List<String[]>>> getMembersDistribution(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getMembersDistribution(GymBranchId.of(gymId, branchId)));
  }


}
