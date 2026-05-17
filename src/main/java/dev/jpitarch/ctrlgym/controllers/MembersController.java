package dev.jpitarch.ctrlgym.controllers;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.services.MembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class MembersController {

  private final MembersService membersService;

  @PostMapping(value = "/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt) throws WriterException, IOException {
    byte[] qrImage = membersService.generateQrCode(jwt.getClaims().get("email").toString());
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }
}
