package dev.jpitarch.ctrlgym.authentication.repositories;

import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final UserJpaRepository jpaRepository;

  public Optional<UserEntity> findByEmail(String email) {
    return jpaRepository.findByEmail(email);
  }

  public Optional<UserEntity> findByEmailAndGymId(String email, Integer gymId) {
    return jpaRepository.findByEmailAndGymId(email, gymId);
  }

  public UserEntity findById(UUID id, Integer gymId) {
    var compositeId = new UserEntity.ID(id, gymId);
    return jpaRepository.findById(compositeId).orElse(null);
  }

  public void save(UserEntity user) {
    jpaRepository.save(user);
  }

  public UserEntity create(String email, String hashedPassword, Integer gymId, String name, String firstSurname, String secondSurname) {
    var UserEntity = new UserEntity();
    UserEntity.setId(UUID.randomUUID());
    UserEntity.setGymId(gymId);
    UserEntity.setEmail(email);
    UserEntity.setPassword(hashedPassword);
    UserEntity.setName(name);
    UserEntity.setStatus(UserStatus.AUTH);
    UserEntity.setFirstSurname(firstSurname);
    UserEntity.setSecondSurname(secondSurname);
    UserEntity.setRole(Role.MEMBER);
    return jpaRepository.save(UserEntity);
  }
}
