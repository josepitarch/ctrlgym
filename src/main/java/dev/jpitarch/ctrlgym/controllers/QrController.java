package dev.jpitarch.ctrlgym.controllers;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.services.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class QrController {

  private final QrService qrService;

  @PostMapping(value = "/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr() throws WriterException, IOException {
    byte[] qrImage = qrService.generateQrCode();
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }
}
