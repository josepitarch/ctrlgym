package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

  private Id id;

  private String name;

  @JsonProperty("first_surname")
  private String firstSurname;

  @JsonProperty("second_surname")
  private String secondSurname;

  private String email;

  private Gender gender;

  @JsonProperty("birth_date")
  private LocalDate birthDate;

  private Address address;

  @JsonIgnore
  public String getFullName() {
    return name + " " + firstSurname + " " + secondSurname;
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

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Address {

    private String street;

    private String city;

    private String state;

    @JsonProperty("postal_code")
    private Integer postalCode;

    private String country;

  }

}
