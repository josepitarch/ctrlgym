package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MemberAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAccessJpaRepository extends JpaRepository<MemberAccess, Integer> {

  List<MemberAccess> findByMemberId(UUID memberId);
}
