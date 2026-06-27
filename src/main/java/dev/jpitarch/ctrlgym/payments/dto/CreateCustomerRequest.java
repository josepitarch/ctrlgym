package dev.jpitarch.ctrlgym.payments.dto;

import dev.jpitarch.ctrlgym.core.domain.Member;

public record CreateCustomerRequest(
  Member.Id memberId,
  String nif,
  String name,
  String firstSurname,
  String secondSurname,
  String address,
  String city,
  Integer postalCode,
  String state,
  String country
) {
  public String getFullName() {
    return name + " " + firstSurname + " " + secondSurname;
  }
}
