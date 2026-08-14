package dev.jpitarch.ctrlgym.storage;

import dev.jpitarch.ctrlgym.storage.config.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  private final S3Client r2Client;
  private final R2Properties properties;

  public String uploadFile(MultipartFile file, String folder) {
    String key = generateKey(file.getOriginalFilename(), folder);

    try {
      var request = PutObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .contentType(file.getContentType())
        .contentLength(file.getSize())
        .build();

      r2Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      log.info("File uploaded successfully: {}", key);
      return properties.publicUrl() + "/" + key;
    } catch (IOException e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw new StorageException("Failed to upload file", e);
    }
  }

  public String uploadFile(byte[] content, String filename, String contentType, String folder) {
    String key = generateKey(filename, folder);

    var request = PutObjectRequest.builder()
      .bucket(properties.bucket())
      .key(key)
      .contentType(contentType)
      .contentLength((long) content.length)
      .build();

    r2Client.putObject(request, RequestBody.fromBytes(content));

    log.info("File uploaded successfully: {}", key);
    return properties.publicUrl() + "/" + key;
  }

  public void deleteFile(String fileUrl) {
    String key = extractKeyFromUrl(fileUrl);

    var request = DeleteObjectRequest.builder()
      .bucket(properties.bucket())
      .key(key)
      .build();

    r2Client.deleteObject(request);
    log.info("File deleted successfully: {}", key);
  }

  private String generateKey(String originalFilename, String folder) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return folder + "/" + UUID.randomUUID() + extension;
  }

  private String extractKeyFromUrl(String fileUrl) {
    return fileUrl.replace(properties.publicUrl() + "/", "");
  }
}
