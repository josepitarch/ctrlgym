package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.core.dto.CashFlow;
import dev.jpitarch.ctrlgym.core.dto.MembersDistribution;
import dev.jpitarch.ctrlgym.core.dto.RetentionVsChurn;
import dev.jpitarch.ctrlgym.core.repositories.*;
import dev.jpitarch.ctrlgym.core.dto.MembershipSeniorityDistribution;
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

  private final MembershipsRepository membershipsRepository;

  private final ExpensesRepository expensesRepository;

  private final InvoicesRepository invoicesRepository;

  private final MembersRepository membersRepository;

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  private final MessageSource messageSource;

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
      new MembersDistribution.Item(toPostalCodeList(distribution.get(MembersDistribution.Group.POSTAL_CODE))),
      new MembersDistribution.Item(toAgeList(distribution.get(MembersDistribution.Group.AGE))),
      new MembersDistribution.Item(toGenderList(distribution.get(MembersDistribution.Group.GENDER))),
      new MembershipSeniorityDistribution(toSeniorityList(seniority.data()))
    );
  }

  private List<Object[]> toPostalCodeList(List<String[]> entries) {
    if (entries == null) return Collections.emptyList();
    return entries.stream()
      .map(e -> new Object[]{
        postalCodeJpaRepository.findByPostalCode(e[0])
          .map(PostalCodeMO::getCity)
          .orElse(e[0]),
        Integer.parseInt(e[1])
      })
      .toList();
  }

  private List<Object[]> toAgeList(List<String[]> entries) {
    if (entries == null) return Collections.emptyList();
    var locale = LocaleContextHolder.getLocale();
    return entries.stream()
      .map(e -> new Object[]{resolveAgeLabel(e[0], locale), Integer.parseInt(e[1])})
      .toList();
  }

  private String resolveAgeLabel(String key, java.util.Locale locale) {
    return switch (key) {
      case "18-25" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{18, 25}, locale);
      case "26-35" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{26, 35}, locale);
      case "36-45" -> messageSource.getMessage("dashboard.members.distribution.between-years", new Object[]{36, 45}, locale);
      case "+45" -> messageSource.getMessage("dashboard.members.distribution.plus-year", new Object[]{45}, locale);
      default -> key;
    };
  }

  private List<Object[]> toGenderList(List<String[]> entries) {
    if (entries == null) return Collections.emptyList();
    var locale = LocaleContextHolder.getLocale();
    return entries.stream()
      .map(e -> new Object[]{resolveGenderLabel(e[0], locale), Integer.parseInt(e[1])})
      .toList();
  }

  private String resolveGenderLabel(String key, java.util.Locale locale) {
    return switch (key.toUpperCase()) {
      case "M" -> messageSource.getMessage("dashboard.members.distribution.male", null, locale);
      case "F" -> messageSource.getMessage("dashboard.members.distribution.female", null, locale);
      default -> key;
    };
  }

  private List<Object[]> toSeniorityList(List<Object[]> seniority) {
    if (seniority == null) return Collections.emptyList();
    return seniority.stream()
      .map(e -> new Object[]{resolveSeniorityLabel((String) e[0]), e[1]})
      .toList();
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
