package dev.jpitarch.ctrlgym.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "storage.r2")
public record R2Properties(
  String accountId,
  String accessKeyId,
  String secretAccessKey,
  String bucket,
  String publicUrl
) {
  public URI endpoint() {
    return URI.create("https://%s.r2.cloudflarestorage.com".formatted(accountId));
  }
}
