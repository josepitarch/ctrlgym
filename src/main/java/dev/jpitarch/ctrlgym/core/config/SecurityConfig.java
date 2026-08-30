package dev.jpitarch.ctrlgym.core.config;

import dev.jpitarch.ctrlgym.core.controllers.filters.ControllerApiKeyFilter;
import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public FilterRegistrationBean<ControllerApiKeyFilter> apiKeyFilterRegistration(ControllerApiKeyFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http, ControllerApiKeyFilter filter) {
    http
      .cors(Customizer.withDefaults())
      .securityMatcher("/v1/controllers/**")
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**", "/v1/payments/webhook", "/v1/auth/**", "/v1/gyms/*/routines", "/health").permitAll()
        .requestMatchers("/v1/dashboard/**").hasRole("MANAGER")
        .requestMatchers("/v1/members/**").hasRole("MEMBER")
        .requestMatchers("/admin/**").hasRole("MANAGER")
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth -> oauth.jwt(
        jwt -> jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
      ));

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

  @Bean
  public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String jwtSecret) {
    var secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    return NimbusJwtDecoder.withSecretKey(secretKey)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }

}
