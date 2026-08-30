package dev.jpitarch.ctrlgym.core.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    Integer gymId = extractGymId(jwt);

    return new CustomJwtAuthenticationToken(jwt, authorities, gymId);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    String role = jwt.getClaimAsString("role");

    if (role == null) return Collections.emptyList();

    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
  }

  private Integer extractGymId(Jwt jwt) {
    Object gymIdObj = jwt.getClaim("gym_id");
    if (gymIdObj instanceof Integer) {
      return (Integer) gymIdObj;
    } else if (gymIdObj instanceof Number) {
      return ((Number) gymIdObj).intValue();
    }

    return null;
  }
}
