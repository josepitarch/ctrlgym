package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Member extends User {

  private String nif;

  @JsonProperty("birth_date")
  private LocalDate birthDate;

  private String iban;

  @JsonProperty("is_active")
  private boolean isActive;

  private Address address;

  private MemberStatus status;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Address {

    private String city;

    @JsonProperty("postal_code")
    private Integer postalCode;

  }

}
