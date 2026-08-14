package dev.jpitarch.ctrlgym.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

  @Bean
  public S3Client r2Client(R2Properties properties) {
    return S3Client.builder()
      .endpointOverride(properties.endpoint())
      .region(Region.EU_WEST_1)
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())
      ))
      .build();
  }

  @Bean
  public S3Presigner r2Presigner(R2Properties properties) {
    return S3Presigner.builder()
      .endpointOverride(properties.endpoint())
      .region(Region.EU_WEST_1)
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())
      ))
      .build();
  }
}
