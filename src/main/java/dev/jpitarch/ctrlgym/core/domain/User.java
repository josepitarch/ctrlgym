package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Data
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

  private UUID id;

  @JsonProperty("avatar_url")
  private URI avatarUrl;

  private String name;

  @JsonProperty("first_surname")
  private String firstSurname;

  @JsonProperty("second_surname")
  private String secondSurname;

  private String email;

  private Gender gender;

  @JsonIgnore
  public String getFullName() {
    return name + " " + firstSurname + " " + Optional.ofNullable(secondSurname).orElse("");
  }

}
