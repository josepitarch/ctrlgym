package dev.jpitarch.ctrlgym.domain.enums;

import lombok.Getter;

@Getter
public enum Gender {
  MALE("M"),
  FEMALE("F");

  private final String code;

  Gender(String code) {
    this.code = code;
  }

  public static Gender fromCode(String code) {
    for (Gender gender : Gender.values()) {
      if (gender.code.equals(code)) {
        return gender;
      }
    }
    throw new IllegalArgumentException("Invalid Gender code: " + code);
  }

}
