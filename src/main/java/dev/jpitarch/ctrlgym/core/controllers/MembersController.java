package dev.jpitarch.ctrlgym.core.controllers;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.services.MembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
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

  @GetMapping(value = "/members/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable UUID memberId, @PathVariable UUID invoiceId) {
    byte[] pdfReport = membersService.getInvoiceReport(memberId, invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }

}
