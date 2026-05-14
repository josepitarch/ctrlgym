package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.domain.GymBranchId;
import dev.jpitarch.ctrlgym.domain.enums.RangePeriod;
import dev.jpitarch.ctrlgym.usecases.DashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardUseCase useCase;

  @GetMapping("/dashboard/gyms/{gymId}/branches/{branchId}")
  public List<String[]> getDailyOccupancy(@PathVariable int gymId, @PathVariable int branchId, @RequestParam RangePeriod rangePeriod) {
    return useCase.getDailyOccupancies(GymBranchId.of(gymId, branchId), rangePeriod);
  }

}
