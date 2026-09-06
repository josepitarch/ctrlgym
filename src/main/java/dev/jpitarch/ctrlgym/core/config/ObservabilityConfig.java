package dev.jpitarch.ctrlgym.core.config;

import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.ObservationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservabilityConfig {

  @Bean
  public ObservationFilter tenantObservationFilter() {
    return context -> {
      if (context instanceof ServerRequestObservationContext) {
        Integer tenant = TenantContextHolder.getTenantId();
        context.addLowCardinalityKeyValue(
          KeyValue.of("tenant", tenant != null ? tenant.toString() : "unknown")
        );
      }
      return context;
    };
  }

}
