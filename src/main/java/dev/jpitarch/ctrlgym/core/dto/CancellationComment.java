package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record CancellationComment(
  @JsonProperty("reason_id") int reasonId,
  @JsonProperty("member") Member member,
  @JsonProperty("comment") String comment
) {

  public record Member(
    @JsonProperty("name") String name,
    @JsonProperty("first_surname") String firstSurname,
    @JsonProperty("second_surname") String secondSurname,
    @JsonProperty("avatar_url") URI avatarUrl
  ) {
  }
}
