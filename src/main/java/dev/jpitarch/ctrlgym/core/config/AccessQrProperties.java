package dev.jpitarch.ctrlgym.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "access")
public class  AccessQrProperties {

  private Duration entry;

  private Duration exit;

}
