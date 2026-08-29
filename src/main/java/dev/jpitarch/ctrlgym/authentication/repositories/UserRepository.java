package dev.jpitarch.ctrlgym.authentication.repositories;

import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final UserJpaRepository jpaRepository;

  public UserMO findByEmail(String email) {
    return jpaRepository.findByEmail(email).orElse(null);
  }

  public void save(UserMO user) {
    jpaRepository.save(user);
  }

  public UserMO create(String email, String hashedPassword, Integer gymId, String name, String firstSurname, String secondSurname) {
    var userMO = new UserMO();
    userMO.setId(UUID.randomUUID());
    userMO.setGymId(gymId);
    userMO.setEmail(email);
    userMO.setPassword(hashedPassword);
    userMO.setName(name);
    userMO.setStatus(MemberStatus.AUTH);
    userMO.setFirstSurname(firstSurname);
    userMO.setSecondSurname(secondSurname);
    userMO.setRole(Role.MEMBER);
    return jpaRepository.save(userMO);
  }
}
