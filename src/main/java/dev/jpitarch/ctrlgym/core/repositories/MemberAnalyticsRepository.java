package dev.jpitarch.ctrlgym.core.repositories;


import dev.jpitarch.ctrlgym.core.dto.ExercisePR;
import dev.jpitarch.ctrlgym.core.dto.MonthlyVolume;
import dev.jpitarch.ctrlgym.core.dto.PRProgress;
import dev.jpitarch.ctrlgym.core.dto.StreakInfo;
import dev.jpitarch.ctrlgym.core.dto.WeeklyVisits;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberAnalyticsRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public Optional<StreakInfo> getCurrentStreak(UUID memberId) {
    var sql = """
      WITH dias_entrenados AS (
        SELECT DISTINCT member_id, date_trunc('day', created_at) AS dia
        FROM member_accesses
        WHERE direction = 0 AND member_id = :memberId
      ),
      grupos AS (
        SELECT dia,
               dia - (ROW_NUMBER() OVER (ORDER BY dia))::int * interval '1 day' AS grupo
        FROM dias_entrenados
      )
      SELECT MAX(dia) AS last_day, COUNT(*) AS streak
      FROM grupos
      WHERE grupo = (SELECT grupo FROM grupos ORDER BY dia DESC LIMIT 1)
      GROUP BY grupo
      """;

    var params = Map.of("memberId", memberId);

    var results = jdbc.query(sql, params, (rs, rowNum) -> new StreakInfo(
      rs.getDate("last_day").toLocalDate(),
      rs.getInt("streak")
    ));

    return results.stream().findFirst();
  }

  public List<WeeklyVisits> getWeeklyVisits(UUID memberId) {
    var sql = """
      SELECT date_trunc('week', created_at) AS week, COUNT(DISTINCT date_trunc('day', created_at)) AS visits
      FROM member_accesses
      WHERE direction = 0 AND member_id = :memberId
      GROUP BY 1 ORDER BY 1
      """;

    var params = Map.of("memberId", memberId);

    return jdbc.query(sql, params, (rs, rowNum) -> new WeeklyVisits(
      rs.getDate("week").toLocalDate(),
      rs.getInt("visits")
    ));
  }

  public List<ExercisePR> getExercisePRs(UUID memberId) {
    var sql = """
      SELECT ws.exercise_id, MAX(ws.weight) AS pr_weight, MAX(ws.reps) AS pr_reps
      FROM workout_sets ws
      JOIN workouts w ON w.id = ws.workout_id
      WHERE w.member_id = :memberId
      GROUP BY ws.exercise_id
      """;

    var params = Map.of("memberId", memberId);

    return jdbc.query(sql, params, (rs, rowNum) -> new ExercisePR(
      UUID.fromString(rs.getString("exercise_id")),
      rs.getDouble("pr_weight"),
      rs.getInt("pr_reps")
    ));
  }

  public List<PRProgress> getPRProgress(UUID memberId, UUID exerciseId) {
    var sql = """
      SELECT w.started_at::date AS date, ws.exercise_id, MAX(ws.weight * ws.reps) AS best_set
      FROM workout_sets ws
      JOIN workouts w ON w.id = ws.workout_id
      WHERE w.member_id = :memberId AND ws.exercise_id = :exerciseId
      GROUP BY 1, 2 ORDER BY 1
      """;

    var params = Map.of("memberId", memberId, "exerciseId", exerciseId);

    return jdbc.query(sql, params, (rs, rowNum) -> new PRProgress(
      rs.getDate("date").toLocalDate(),
      UUID.fromString(rs.getString("exercise_id")),
      rs.getDouble("best_set")
    ));
  }

  public List<MonthlyVolume> getMonthlyVolume(UUID memberId) {
    var sql = """
      SELECT date_trunc('month', w.started_at) AS month, SUM(ws.weight * ws.reps) AS total_volume
      FROM workout_sets ws
      JOIN workouts w ON w.id = ws.workout_id
      WHERE w.member_id = :memberId
      GROUP BY 1 ORDER BY 1
      """;

    var params = Map.of("memberId", memberId);

    return jdbc.query(sql, params, (rs, _) -> new MonthlyVolume(
      YearMonth.from(rs.getDate("month").toLocalDate()),
      rs.getDouble("total_volume")
    ));
  }
}
