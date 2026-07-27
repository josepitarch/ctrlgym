package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.UserMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserMO, UserMO.ID> {

  @Query("""
        SELECT m.id
        FROM UserMO m
        WHERE m.email = :email
        LIMIT 1
    """)
  Optional<UUID> findIdByEmail(String email);
}
