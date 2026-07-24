package dev.jpitarch.ctrlgym.core.config;

import dev.jpitarch.ctrlgym.core.controllers.filters.ControllerApiKeyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
      .requestMatchers("/public/**", "/v1/payments/webhook", "/health").permitAll()
      .requestMatchers("/v1/dashboard/**").hasRole("MANAGER")
      .requestMatchers("/v1/gyms/**").hasAnyRole("MANAGER", "EMPLOYEE")
      .anyRequest().authenticated()
    )
    .oauth2ResourceServer(oauth -> oauth.jwt(
            jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()
            )
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

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    return converter;
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("user_roles");

    if (CollectionUtils.isEmpty(roles)) return Collections.emptyList();

    return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
  }
}
