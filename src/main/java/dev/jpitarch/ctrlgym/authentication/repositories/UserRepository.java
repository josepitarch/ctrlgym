package dev.jpitarch.ctrlgym.authentication.repositories;

import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final UserJpaRepository jpaRepository;

  public UserEntity findByEmail(String email) {
    return jpaRepository.findByEmail(email).orElse(null);
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
    UserEntity.setStatus(MemberStatus.AUTH);
    UserEntity.setFirstSurname(firstSurname);
    UserEntity.setSecondSurname(secondSurname);
    UserEntity.setRole(Role.MEMBER);
    return jpaRepository.save(UserEntity);
  }
}
