package dev.jpitarch.ctrlgym.verifactu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder
      .baseUrl("https://api.verifacti.com/verifactu")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .build();
  }
}
