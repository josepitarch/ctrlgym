package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Employee;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.models.EmployeeWorkplaceMO;
import dev.jpitarch.ctrlgym.core.models.GymBranchMO;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.EmployeeJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmployeesRepository {

  private final EmployeeJpaRepository jpaRepository;

  private final UserJpaRepository userJpaRepository;

  private final GymJpaRepository gymJpaRepository;

  public void assignToBranch(Member.Id employeeId, Integer gymBranchId) {
    var assignment = new EmployeeWorkplaceMO();
    assignment.setEmployeeId(employeeId.memberId());
    assignment.setGymId(employeeId.gymId());
    assignment.setAllBranches(false);

    GymBranchMO branch = gymJpaRepository.findBranchByGymIdAndBranchId(employeeId.gymId(), gymBranchId);
    if (branch == null) {
      throw new IllegalArgumentException("Gym branch not found");
    }
    assignment.setGymBranch(branch);

    jpaRepository.save(assignment);
  }

  public void assignToAllBranches(Member.Id employeeId) {
    var assignment = new EmployeeWorkplaceMO();
    assignment.setEmployeeId(employeeId.memberId());
    assignment.setGymId(employeeId.gymId());
    assignment.setAllBranches(true);

    jpaRepository.save(assignment);
  }

  public List<Employee> getEmployees(GymBranchId gymBranchId) {
    List<EmployeeWorkplaceMO> assignments = jpaRepository.findByGymIdAndGymBranchIdAndAllBranchesFalse(
      gymBranchId.gymId(),
      gymBranchId.branchId()
    );

    return assignments.stream()
      .map(assignment -> {
        UserMO userMO = userJpaRepository.findById(
          new UserMO.ID(assignment.getEmployeeId(), assignment.getGymId())
        ).orElseThrow(() -> new MemberNotFoundException(Member.Id.of(assignment.getEmployeeId(), assignment.getGymId())));

        Employee employee = Employee.builder()
          .id(Member.Id.of(userMO.getId(), userMO.getGymId()))
          .name(userMO.getName())
          .firstSurname(userMO.getFirstSurname())
          .secondSurname(userMO.getSecondSurname())
          .email(userMO.getEmail())
          .gender(mapGender(userMO.getGender()))
          .build();
        return employee;
      })
      .toList();
  }

  private String mapGender(Gender gender) {
    return switch (gender) {
      case MALE -> "M";
      case FEMALE -> "F";
    };
  }

  private Gender mapGender(String gender) {
    return switch (gender) {
      case "M" -> Gender.MALE;
      case "F" -> Gender.FEMALE;
      case null -> null;
      default -> throw new IllegalStateException("Unexpected value: " + gender);
    };
  }

}
