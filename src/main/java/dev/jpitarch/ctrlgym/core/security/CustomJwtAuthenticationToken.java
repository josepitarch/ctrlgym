package dev.jpitarch.ctrlgym.core.security;

import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {

  private final Integer gymId;

  public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, Integer gymId) {
    super(jwt, authorities);
    this.gymId = gymId;
  }

}
