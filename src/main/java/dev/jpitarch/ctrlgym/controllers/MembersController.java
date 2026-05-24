package dev.jpitarch.ctrlgym.controllers;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.domain.MemberAccess;
import dev.jpitarch.ctrlgym.services.MembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MembersController {

  private final MembersService membersService;

  @GetMapping(value = "/members/{memberId}/accesses")
  public List<MemberAccess> getAccesses(@PathVariable UUID memberId) {
    return membersService.getAccesses(memberId);
  }

  @PostMapping(value = "/members/{memberId}/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId) throws WriterException, IOException {
    byte[] qrImage = membersService.generateQrCode(jwt.getClaims().get("email").toString());
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }
}
