package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MembershipPlanMO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlanMO, String> {
}
