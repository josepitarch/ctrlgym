package dev.jpitarch.ctrlgym.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtSigningConfig {

  @Bean
  public PrivateKey jwtSigningKey(@Value("${qr.jwt-secret}") String privateKeyPem) throws Exception {
    String cleaned = privateKeyPem
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replaceAll("\\s", "");

    byte[] decoded = Base64.getDecoder().decode(cleaned);
    var kf = KeyFactory.getInstance("EC");
    return kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
  }

}
