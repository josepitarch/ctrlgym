package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.domain.DatePeriod;
import dev.jpitarch.ctrlgym.domain.GymBranchId;
import dev.jpitarch.ctrlgym.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.usecases.DashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardUseCase useCase;

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/occupancy")
  public List<String[]> getOccupancies(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam Granularity granularity) {
    return useCase.getOccupancies(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), granularity);
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships")
  public List<String[]> getMemberships(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam MembershipFlow flow) {
    return useCase.getMemberships(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to), flow);
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships/seniority-avg")
  public Integer getMembershipSeniorityAverage(@PathVariable int gymId, @PathVariable int branchId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return useCase.getMembershipSeniorityAverage(GymBranchId.of(gymId, branchId), DatePeriod.of(from, to));
  }

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}/memberships/cohorts")
  public List<String[]> getCohorts(@PathVariable int gymId, @PathVariable int branchId) {
    return useCase.getCohorts(GymBranchId.of(gymId, branchId));
  }

}
