package dev.jpitarch.ctrlgym.usecases;

import dev.jpitarch.ctrlgym.domain.DatePeriod;
import dev.jpitarch.ctrlgym.domain.GymBranchId;
import dev.jpitarch.ctrlgym.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.repositories.GymBranchOccupancyRepository;
import dev.jpitarch.ctrlgym.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymBranchOccupancyRepository gymBranchOccupancyRepository;

  private final MembershipRepository membershipRepository;

  public List<String[]> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    return gymBranchOccupancyRepository.getOccupancies(gymBranchId, datePeriod, granularity);
  }

  public List<String[]> getMemberships(GymBranchId gymBranchId, DatePeriod datePeriod, MembershipFlow flow) {
    return switch (flow) {
      case ACTIVE -> membershipRepository.getCurrentCount(gymBranchId, datePeriod);
      case NEW -> membershipRepository.getNewsCount(gymBranchId, datePeriod);
      case CANCELLED -> membershipRepository.getCancelledCount(gymBranchId, datePeriod);
    };
  }

  public Integer getMembershipSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return membershipRepository.getSeniorityAverage(gymBranchId, datePeriod);
  }

  public List<String[]> getCohorts(GymBranchId gymBranchId) {
    return membershipRepository.getCohorts(gymBranchId);
  }

}
