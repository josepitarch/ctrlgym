package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Employee;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.entities.EmployeeWorkplaceEntity;
import dev.jpitarch.ctrlgym.core.entities.GymBranchEntity;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.EmployeeJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmployeesRepository {

  private final EmployeeJpaRepository jpaRepository;

  private final UserJpaRepository userJpaRepository;

  private final GymJpaRepository gymJpaRepository;

  public UserEntity createEmployee(String email, Integer gymId, String name, String firstSurname, String secondSurname, String gender) {
    var user = new UserEntity();
    user.setId(UUID.randomUUID());
    user.setGymId(gymId);
    user.setEmail(email);
    user.setPassword(null);
    user.setName(name);
    user.setFirstSurname(firstSurname);
    user.setSecondSurname(secondSurname);
    user.setGender(gender);
    user.setStatus(UserStatus.PENDING_ACTIVATION);
    user.setRole(Role.EMPLOYEE);
    return userJpaRepository.save(user);
  }

  public void assignToBranch(UUID employeeId, Integer gymId, Integer gymBranchId) {
    var assignment = new EmployeeWorkplaceEntity();
    assignment.setEmployeeId(employeeId);
    assignment.setGymId(gymId);
    assignment.setAllBranches(false);

    GymBranchEntity branch = gymJpaRepository.findBranchByGymIdAndBranchId(gymId, gymBranchId);
    if (branch == null) {
      throw new IllegalArgumentException("Gym branch not found");
    }
    assignment.setGymBranch(branch);

    jpaRepository.save(assignment);
  }

  public void assignToAllBranches(UUID employeeId, Integer gymId) {
    var assignment = new EmployeeWorkplaceEntity();
    assignment.setEmployeeId(employeeId);
    assignment.setGymId(gymId);
    assignment.setAllBranches(true);

    jpaRepository.save(assignment);
  }

  public List<Employee> getEmployees(GymBranchId gymBranchId) {
    List<EmployeeWorkplaceEntity> assignments = jpaRepository.findByGymIdAndGymBranchIdAndAllBranchesFalse(
      gymBranchId.gymId(),
      gymBranchId.branchId()
    );

    return assignments.stream()
      .map(assignment -> {
        UserEntity userEntity = userJpaRepository.findById(assignment.getEmployeeId())
          .orElseThrow(() -> new MemberNotFoundException(assignment.getEmployeeId()));

        Employee employee = Employee.builder()
          .id(userEntity.getId())
          .name(userEntity.getName())
          .firstSurname(userEntity.getFirstSurname())
          .secondSurname(userEntity.getSecondSurname())
          .email(userEntity.getEmail())
          .gender(mapGender(userEntity.getGender()))
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
