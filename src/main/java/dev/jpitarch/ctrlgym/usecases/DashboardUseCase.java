package dev.jpitarch.ctrlgym.usecases;

import dev.jpitarch.ctrlgym.domain.*;
import dev.jpitarch.ctrlgym.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.domain.enums.MemberDistribution;
import dev.jpitarch.ctrlgym.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymBranchOccupancyRepository gymBranchOccupancyRepository;

  private final MembershipsRepository membershipsRepository;

  private final ExpensesRepository expensesRepository;

  private final InvoicesRepository invoicesRepository;

  private final MembersRepository membersRepository;

  public List<Map<String, Integer>> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    return gymBranchOccupancyRepository.getOccupancies(gymBranchId, datePeriod, granularity);
  }

  public List<Map<YearMonth, Integer>> getMemberships(GymBranchId gymBranchId, DatePeriod datePeriod, MembershipFlow flow) {
    return switch (flow) {
      case ACTIVE -> membershipsRepository.getCurrentCount(gymBranchId, datePeriod);
      case NEW -> membershipsRepository.getNewsCount(gymBranchId, datePeriod);
      case CANCELLED -> membershipsRepository.getCancelledCount(gymBranchId, datePeriod);
    };
  }

  public Integer getMembershipSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return membershipsRepository.getSeniorityAverage(gymBranchId, datePeriod);
  }

  public List<Cohort> getCohorts(GymBranchId gymBranchId) {
    return membershipsRepository.getCohorts(gymBranchId);
  }

  public List<Map<String, Integer>> getCancellationReasons(GymBranchId gymBranchId) {
    return membershipsRepository.getCancellationReasons(gymBranchId, null);
  }

  public List<Expense> getExpenses(GymBranchId gymBranchId) {
    return expensesRepository.getExpenses(gymBranchId);
  }

  public Map<String, List<Map<YearMonth, Double>>> getCashFlow(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var invoices = invoicesRepository.getTotalPerMonth(gymBranchId, datePeriod);
    var expenses = expensesRepository.getTotalPerMonth(gymBranchId, datePeriod);

    return Map.of(
      "EARNINGS", invoices,
      "COSTS", expenses
    );
  }

  public List<Member> getMembers(GymBranchId gymBranchId) {
    return membersRepository.getMembers(gymBranchId);
  }

  public Map<MemberDistribution, List<String[]>> getMembersDistribution(GymBranchId gymBranchId) {
    return membersRepository.getDistribution(gymBranchId);
  }

}
