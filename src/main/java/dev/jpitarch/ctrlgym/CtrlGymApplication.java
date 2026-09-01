package dev.jpitarch.ctrlgym;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
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

  @Slf4j
  @Component
  @Profile("!local & !test")
  static class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
      this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
      log.info("Installing OpenTelemetryAppender...");
      OpenTelemetryAppender.install(this.openTelemetry);
      log.info("Installing OpenTelemetryAppender finished");
    }

  }

}
