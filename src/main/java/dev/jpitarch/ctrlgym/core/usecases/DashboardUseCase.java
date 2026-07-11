package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.core.dto.CashFlow;
import dev.jpitarch.ctrlgym.core.dto.MembersDistribution;
import dev.jpitarch.ctrlgym.core.dto.RetentionVsChurn;
import dev.jpitarch.ctrlgym.core.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymsRepository gymsRepository;

  private final MembershipsRepository membershipsRepository;

  private final ExpensesRepository expensesRepository;

  private final InvoicesRepository invoicesRepository;

  private final MembersRepository membersRepository;

  public List<Map<String, Integer>> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    return gymsRepository.getOccupancies(gymBranchId, datePeriod, granularity);
  }

  public Map<YearMonth, Integer> getMemberships(GymBranchId gymBranchId, DatePeriod datePeriod, MembershipFlow flow) {
    return switch (flow) {
      case ACTIVE -> membershipsRepository.getCurrentCount(gymBranchId, datePeriod);
      case NEW -> membershipsRepository.getNewsCount(gymBranchId, datePeriod);
      case CANCELLED -> membershipsRepository.getCancelledCount(gymBranchId, datePeriod);
    };
  }

  public Map<YearMonth, Integer> getMembershipSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return membershipsRepository.getSeniorityAverage(gymBranchId, datePeriod);
  }

  public List<Cohort> getCohorts(GymBranchId gymBranchId) {
    return membershipsRepository.getCohorts(gymBranchId);
  }

  public RetentionVsChurn getRetentionVsChurn(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return membershipsRepository.getRetentionVsChurn(gymBranchId, datePeriod);
  }

  public List<Map<String, Integer>> getCancellationReasons(GymBranchId gymBranchId) {
    return membershipsRepository.getCancellationReasons(gymBranchId, null);
  }

  public List<Expense> getExpenses(GymBranchId gymBranchId) {
    return expensesRepository.getExpenses(gymBranchId);
  }

  public CashFlow getCashFlow(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var expenses = expensesRepository.getTotalPerMonth(gymBranchId, datePeriod);
    var revenues = invoicesRepository.getTotalPerMonth(gymBranchId, datePeriod);

    return new CashFlow(expenses, revenues);
  }

  public MembersDistribution getMembersDistribution(GymBranchId gymBranchId) {
    var distribution = membersRepository.getDistribution(gymBranchId);
    var seniority = membershipsRepository.getSeniorityDistribution(gymBranchId);
    return new MembersDistribution(
      new MembersDistribution.Item(toMap(distribution.get(MembersDistribution.Group.POSTAL_CODE))),
      new MembersDistribution.Item(toMap(distribution.get(MembersDistribution.Group.AGE))),
      new MembersDistribution.Item(toMap(distribution.get(MembersDistribution.Group.GENDER))),
      seniority
    );
  }

  private Map<String, Integer> toMap(List<String[]> entries) {
    if (entries == null) return Collections.emptyMap();
    return entries.stream().collect(Collectors.toMap(e -> e[0], e -> Integer.parseInt(e[1])));
  }

}
