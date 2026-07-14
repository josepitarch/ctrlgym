package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public record MembersDistribution(@JsonProperty("postal_code") Item postalCode, Item age, Item gender, MembershipSeniorityDistribution seniority) {

  public record Item(@JsonValue List<Object[]> item) {
  }

  public enum Group {
    AGE,
    GENDER,
    POSTAL_CODE

  }

}
