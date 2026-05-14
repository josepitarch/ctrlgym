package dev.jpitarch.ctrlgym.repositories.jpa;

import dev.jpitarch.ctrlgym.models.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessEventJpaRepository extends JpaRepository<AccessEvent, Integer> {
}
