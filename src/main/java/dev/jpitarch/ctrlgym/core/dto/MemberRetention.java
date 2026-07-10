package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.Member;

public record MemberRetention(Member.Id id,
                              Integer score,
                              @JsonProperty("life_time_value") Integer lifeTimeValue,
                              @JsonProperty("active_months") Integer activeMonths,
                              @JsonProperty("attendance_avg") Integer attendanceAvg) {
}
