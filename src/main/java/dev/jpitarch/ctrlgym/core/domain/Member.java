package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import lombok.*;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Member {

  private Id id;

  private String nif;

  @JsonProperty("avatar_url")
  private URI avatarUrl;

  private String name;

  @JsonProperty("first_surname")
  private String firstSurname;

  @JsonProperty("second_surname")
  private String secondSurname;

  private String email;

  private Gender gender;

  @JsonProperty("birth_date")
  private LocalDate birthDate;

  @JsonProperty("is_active")
  private boolean isActive;

  private Address address;

  private MemberStatus status;

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

  }

}
