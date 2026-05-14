package dev.jpitarch.ctrlgym.usecases;

import dev.jpitarch.ctrlgym.domain.GymBranchId;
import dev.jpitarch.ctrlgym.domain.enums.RangePeriod;
import dev.jpitarch.ctrlgym.repositories.GymBranchOccupancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymBranchOccupancyRepository gymBranchOccupancyRepository;

  public List<String[]> getDailyOccupancies(GymBranchId gymBranchId, RangePeriod rangePeriod) {
    return switch (rangePeriod) {
      case DAILY -> gymBranchOccupancyRepository.getDailyOccupancies(gymBranchId);
      case WEEKLY -> gymBranchOccupancyRepository.getWeeklyOccupancies(gymBranchId);
      case MONTHLY -> gymBranchOccupancyRepository.getMonthlyOccupancies(gymBranchId);
    };
  }
}
