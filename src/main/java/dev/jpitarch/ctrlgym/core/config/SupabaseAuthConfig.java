package dev.jpitarch.ctrlgym.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SupabaseAuthConfig {

  @Bean("supabaseAuthRestClient")
  public RestClient supabaseAuthRestClient(
    RestClient.Builder builder,
    @Value("${supabase.url}") String supabaseUrl,
    @Value("${supabase.service-role-key}") String serviceRoleKey
  ) {
    return builder
      .baseUrl(supabaseUrl + "/auth/v1")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .defaultHeader("apikey", serviceRoleKey)
      .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
      .build();
  }
}
