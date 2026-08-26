package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.EmployeeWorkplaceMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<EmployeeWorkplaceMO, EmployeeWorkplaceMO.ID> {

  @Query("""
    SELECT ew FROM EmployeeWorkplaceMO ew
    WHERE ew.gymId = :gymId AND ew.gymBranch.id = :gymBranchId
    AND ew.allBranches IS FALSE
  """)
  List<EmployeeWorkplaceMO> findByGymIdAndGymBranchIdAndAllBranchesFalse(Integer gymId, Integer gymBranchId);

}
