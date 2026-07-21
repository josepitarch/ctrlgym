package dev.jpitarch.ctrlgym.core.config;

import dev.jpitarch.ctrlgym.core.controllers.filters.ControllerApiKeyFilter;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final GymsRepository gymsRepository;

  public SecurityConfig(GymsRepository gymsRepository) {
    this.gymsRepository = gymsRepository;
  }

  @Bean
  public ControllerApiKeyFilter apiKeyAuthFilter() {
    return new ControllerApiKeyFilter(gymsRepository);
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) {
    http
      .securityMatcher("/v1/controllers/**")
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .addFilterBefore(apiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }


  @Bean
  @Order(2)
  SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
    .cors(Customizer.withDefaults())
    .csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**", "/v1/payments/webhook", "/health").permitAll()
            .anyRequest().authenticated()
    )
    .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://app.ctrlgym.es"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
