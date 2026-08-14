package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;

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

  private Id id;

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

  public record Id(@JsonProperty("member_id") UUID memberId, @JsonProperty("gym_id") Integer gymId) {

    public static Id of(UUID id, Integer gymId) {
      return new Id(id, gymId);
    }

    @Override
    public @NonNull String toString() {
      return gymId + "-" + memberId;
    }

  }

}
