package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsersRepository {

  private final UserJpaRepository jpaRepository;

  public Optional<UUID> findIdByEmail(String email) {
    return jpaRepository.findIdByEmail(email);
  }

  public void save(UserMO user) {
    jpaRepository.save(user);
  }
}
