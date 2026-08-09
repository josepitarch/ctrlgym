package dev.jpitarch.ctrlgym.core.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.jpitarch.ctrlgym.core.domain.Member;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateAccessQr {

  private final PrivateKey signingKey;

  @Value("${member-access-qr.expiration-seconds:10}")
  private int expirationSeconds;

  private static final int QR_SIZE = 300;

  public byte[] generateQrCode(Member.Id memberId, List<Integer> branches) throws WriterException, IOException {
    var qrCodeWriter = new QRCodeWriter();
    var data = this.generateQrToken(memberId, branches);
    BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

    var pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    return pngOutputStream.toByteArray();
  }

  private String generateQrToken(Member.Id memberId, List<Integer> gymIds) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(memberId.memberId().toString())
      .claim("gym_branches", gymIds)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plusSeconds(expirationSeconds)))
      .signWith(signingKey, Jwts.SIG.ES256)
      .compact();
  }

}
