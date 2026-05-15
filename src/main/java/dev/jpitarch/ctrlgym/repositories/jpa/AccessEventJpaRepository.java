package dev.jpitarch.ctrlgym.repositories.jpa;

import dev.jpitarch.ctrlgym.models.MemberAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessEventJpaRepository extends JpaRepository<MemberAccess, Integer> {
}
