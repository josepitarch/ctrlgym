package dev.jpitarch.ctrlgym.core.components;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class InvoiceCounterComponent {

  private final NamedParameterJdbcTemplate jdbc;

  public Integer nextNumber(Integer gymId, String series) {
    String sql = """
      INSERT INTO invoice_counter (gym_id, series, last_number)
      VALUES (:gymId, :series, 1)
      ON CONFLICT (gym_id, series)
      DO UPDATE SET last_number = invoice_counter.last_number + 1
      RETURNING last_number
      """;

    var params = Map.of(
      "gymId", gymId,
      "series", series
    );

    return jdbc.queryForObject(sql, params, Integer.class);
  }

}
