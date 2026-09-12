package dev.jpitarch.ctrlgym.storage.services;

import dev.jpitarch.ctrlgym.storage.config.R2Properties;
import dev.jpitarch.ctrlgym.storage.config.StorageBucket;
import dev.jpitarch.ctrlgym.storage.exceptions.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  private static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);

  private final S3Client r2Client;
  private final S3Presigner r2Presigner;
  private final R2Properties properties;

  public String uploadFile(MultipartFile file, Integer tenant, String folder, StorageBucket bucket) {
    String key = generateKey(file.getOriginalFilename(), tenant, folder);
    return doUpload(file, key, bucket);
  }

  public String uploadFile(MultipartFile file, Integer tenant, String folder, String customFilename, StorageBucket bucket) {
    String key = generateKeyWithCustomName(file.getOriginalFilename(), tenant, folder, customFilename);
    return doUpload(file, key, bucket);
  }

  private String doUpload(MultipartFile file, String key, StorageBucket bucket) {
    try {
      var bucketConfig = resolveBucket(bucket);
      var request = PutObjectRequest.builder()
        .bucket(bucketConfig.name())
        .key(key)
        .contentType(file.getContentType())
        .contentLength(file.getSize())
        .build();

      r2Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      log.info("File uploaded successfully: {} to bucket {}", key, bucketConfig.name());

      if (bucket == StorageBucket.ASSETS) {
        return bucketConfig.publicUrl() + "/" + key;
      }
      return key;
    } catch (IOException e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw new StorageException("Failed to upload file", e);
    }
  }

  public void deleteFile(String fileUrlOrKey, StorageBucket bucket) {
    var bucketConfig = resolveBucket(bucket);
    String key = extractKey(fileUrlOrKey, bucketConfig);

    var request = DeleteObjectRequest.builder()
      .bucket(bucketConfig.name())
      .key(key)
      .build();

    r2Client.deleteObject(request);
    log.info("File deleted successfully: {} from bucket {}", key, bucketConfig.name());
  }

  public String generatePresignedUrl(String key, StorageBucket bucket) {
    var bucketConfig = resolveBucket(bucket);
    var getObjectRequest = GetObjectRequest.builder()
      .bucket(bucketConfig.name())
      .key(key)
      .build();

    var presignRequest = GetObjectPresignRequest.builder()
      .signatureDuration(PRESIGNED_URL_DURATION)
      .getObjectRequest(getObjectRequest)
      .build();

    var presignedRequest = r2Presigner.presignGetObject(presignRequest);
    return presignedRequest.url().toString();
  }

  private R2Properties.BucketConfig resolveBucket(StorageBucket bucket) {
    return switch (bucket) {
      case ASSETS -> properties.buckets().assets();
      case AVATARS -> properties.buckets().avatars();
    };
  }

  private String extractKey(String fileUrlOrKey, R2Properties.BucketConfig bucketConfig) {
    if (fileUrlOrKey.startsWith(bucketConfig.publicUrl())) {
      return fileUrlOrKey.replace(bucketConfig.publicUrl() + "/", "");
    }
    return fileUrlOrKey;
  }

  private String generateKey(String originalFilename, Integer tenant, String folder) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return "tenants/" + tenant + "/" + folder + "/" + UUID.randomUUID() + extension;
  }

  private String generateKeyWithCustomName(String originalFilename, Integer tenant, String folder, String customFilename) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return "tenants/" + tenant + "/" + folder + "/" + customFilename + extension;
  }
}
