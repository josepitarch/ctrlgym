package dev.jpitarch.ctrlgym.core.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateAccessQrService {

  @Value("${qr.jwt-secret}")
  private String secret;

  @Value("${qr.expiration-seconds}")
  private int expirationSeconds;

  private final MembershipsRepository membershipsRepository;

  private static final int QR_SIZE = 300;

  public byte[] generateQrCode(Member.Id memberId) throws WriterException, IOException {
    var memberships = membershipsRepository.getMembership(memberId);
    if (CollectionUtils.isEmpty(memberships)) {
      throw new IllegalStateException("Member has no accesses for the gym");
    }
    var qrCodeWriter = new QRCodeWriter();
    var data = this.generateQrToken(memberId, memberships);
    System.out.println(data);
    BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

    var pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    return pngOutputStream.toByteArray();
  }

  //TODO: hacer firma asimétrica
  private String generateQrToken(Member.Id memberId, List<Integer> gymIds) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(memberId.id().toString())
      .claim("gym_branches", gymIds)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plusSeconds(expirationSeconds)))
      .signWith(getSigningKey(), Jwts.SIG.HS256)
      .compact();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

}
