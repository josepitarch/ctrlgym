package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateMemberRequest {

  private String name;

  @JsonProperty("first_surname")
  private String firstSurname;

  @JsonProperty("second_surname")
  private String secondSurname;

  private String email;

  private Gender gender;

  @JsonProperty("birth_date")
  private LocalDate birthDate;

  private String nif;

  private Address address;

  @JsonProperty("accepted_document_version_ids")
  List<UUID> acceptedDocumentVersionIds;

  @Getter
  @Setter
  public static class Address {

    private String city;

    @JsonProperty("postal_code")
    private Integer postalCode;
  }
}
