package dev.jpitarch.ctrlgym;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@EnableScheduling
@SpringBootApplication
@EnableResilientMethods
public class CtrlGymApplication {

  static void main(String[] args) {
    SpringApplication.run(CtrlGymApplication.class, args);
  }

  @Component
  @ConditionalOnBean(OpenTelemetry.class)
  static class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
      this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
      OpenTelemetryAppender.install(this.openTelemetry);
    }

  }

}
