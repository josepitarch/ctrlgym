package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.core.dto.CashFlow;
import dev.jpitarch.ctrlgym.core.dto.DistributionItem;
import dev.jpitarch.ctrlgym.core.dto.MembersDistribution;
import dev.jpitarch.ctrlgym.core.dto.OccupancyGranularity;
import dev.jpitarch.ctrlgym.core.dto.RetentionVsChurn;
import dev.jpitarch.ctrlgym.core.repositories.*;
import dev.jpitarch.ctrlgym.core.models.PostalCodeMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.PostalCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymsRepository gymsRepository;

  private final AnalyticsRepository analyticsRepository;

  private final ExpensesRepository expensesRepository;

  private final InvoicesRepository invoicesRepository;

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  private final MessageSource messageSource;

  public OccupancyGranularity getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    var dataPoints = gymsRepository.getOccupancies(gymBranchId, datePeriod, granularity);
    return new OccupancyGranularity(granularity, dataPoints);
  }

  public Map<YearMonth, Integer> getMemberships(GymBranchId gymBranchId, DatePeriod datePeriod, MembershipFlow flow) {
    return switch (flow) {
      case ACTIVE -> analyticsRepository.getCurrentCount(gymBranchId, datePeriod);
      case NEW -> analyticsRepository.getNewsCount(gymBranchId, datePeriod);
      case CANCELLED -> analyticsRepository.getCancelledCount(gymBranchId, datePeriod);
    };
  }

  public Map<YearMonth, Integer> getMembershipSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return analyticsRepository.getSeniorityAverage(gymBranchId, datePeriod);
  }

  public List<Cohort> getCohorts(GymBranchId gymBranchId) {
    return analyticsRepository.getCohorts(gymBranchId);
  }

  public RetentionVsChurn getRetentionVsChurn(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return analyticsRepository.getRetentionVsChurn(gymBranchId, datePeriod);
  }

  public List<Map<String, Integer>> getCancellationReasons(GymBranchId gymBranchId) {
    return analyticsRepository.getCancellationReasons(gymBranchId, null);
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
    var distribution = analyticsRepository.getDistribution(gymBranchId);
    var seniority = analyticsRepository.getSeniorityDistribution(gymBranchId);
    return new MembersDistribution(
      toStringDistributionItemList(distribution.get(MembersDistribution.Group.POSTAL_CODE), this::resolvePostalCode),
      toStringDistributionItemList(distribution.get(MembersDistribution.Group.AGE), this::resolveAgeLabel),
      toStringDistributionItemList(distribution.get(MembersDistribution.Group.GENDER), this::resolveGenderLabel),
      toObjectDistributionItemList(seniority, this::resolveSeniorityLabel)
    );
  }

  private List<DistributionItem> toStringDistributionItemList(List<String[]> entries, java.util.function.Function<String, String> labelMapper) {
    if (entries == null) return Collections.emptyList();
    return entries.stream()
      .map(e -> new DistributionItem(labelMapper.apply(e[0]), Integer.parseInt(e[1])))
      .toList();
  }

  private List<DistributionItem> toObjectDistributionItemList(List<Object[]> entries, java.util.function.Function<String, String> labelMapper) {
    if (entries == null) return Collections.emptyList();
    return entries.stream()
      .map(e -> new DistributionItem(labelMapper.apply((String) e[0]), ((Number) e[1]).intValue()))
      .toList();
  }

  private String resolvePostalCode(String postalCode) {
    return postalCodeJpaRepository.findByPostalCode(postalCode)
      .map(PostalCodeMO::getCity)
      .orElse(postalCode);
  }

  private String resolveAgeLabel(String key) {
    var locale = LocaleContextHolder.getLocale();
    return switch (key) {
      case "18-25" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{18, 25}, locale);
      case "26-35" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{26, 35}, locale);
      case "36-45" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{36, 45}, locale);
      case "+45" -> messageSource.getMessage("dashboard.members.distribution.plus-year", new Object[]{45}, locale);
      default -> key;
    };
  }

  private String resolveGenderLabel(String key) {
    var locale = LocaleContextHolder.getLocale();
    return switch (key.toUpperCase()) {
      case "M" -> messageSource.getMessage("dashboard.members.distribution.male", null, locale);
      case "F" -> messageSource.getMessage("dashboard.members.distribution.female", null, locale);
      default -> key;
    };
  }

  private String resolveSeniorityLabel(String key) {
    var locale = LocaleContextHolder.getLocale();
    return switch (key) {
      case "-1m" -> messageSource.getMessage("dashboard.members.distribution.less-month", new Object[]{1}, locale);
      case "1-3" -> messageSource.getMessage("dashboard.members.distribution.between-months", new Object[]{1, 3}, locale);
      case "4-5m" -> messageSource.getMessage("dashboard.members.distribution.between-months", new Object[]{4, 5}, locale);
      case "6-12m" -> messageSource.getMessage("dashboard.members.distribution.between-months", new Object[]{6, 12}, locale);
      case "1-2y" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{1, 2}, locale);
      case "2-3y" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{2, 3}, locale);
      case "+3y" -> messageSource.getMessage("dashboard.members.distribution.plus-year", new Object[]{3}, locale);
      default -> key;
    };
  }

}
