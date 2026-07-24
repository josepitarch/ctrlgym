package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MembersDistribution(@JsonProperty("postal_code") List<DistributionItem> postalCode,
                                  List<DistributionItem> age, List<DistributionItem> gender,
                                  List<DistributionItem> seniority) {

  public enum Group {
    AGE,
    GENDER,
    POSTAL_CODE

  }

}
