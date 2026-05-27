package dev.jpitarch.ctrlgym.core.domain;

import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

  private UUID id;

  private String name;

  private String firstSurname;

  private String lastSurname;

  private String email;

  private Gender gender;

  private LocalDate birthDate;

  private Integer postalCode;

}
