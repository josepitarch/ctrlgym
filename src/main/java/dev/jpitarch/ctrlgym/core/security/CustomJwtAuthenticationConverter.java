package dev.jpitarch.ctrlgym.core.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    Integer gymId = extractGymId(jwt);

    return new CustomJwtAuthenticationToken(jwt, authorities, gymId);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("user_roles");

    if (CollectionUtils.isEmpty(roles)) return Collections.emptyList();

    return roles.stream()
      .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
      .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Integer extractGymId(Jwt jwt) {
    Map<String, Object> userMetadata = jwt.getClaim("user_metadata");

    if (userMetadata == null || !userMetadata.containsKey("gym_id")) {
      return null;
    }

    Object gymIdObj = userMetadata.get("gym_id");
    if (gymIdObj instanceof Integer) {
      return (Integer) gymIdObj;
    } else if (gymIdObj instanceof Number) {
      return ((Number) gymIdObj).intValue();
    }

    return null;
  }
}
