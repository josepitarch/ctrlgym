package dev.jpitarch.ctrlgym.core.services;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembersService {

  private final MembersRepository membersRepository;

  private final GenerateAccessQrService generateAccessQrService;

  private final GenerateInvoiceReportService generateInvoiceReportService;


  public byte[] generateQrCode(UUID memberId, Integer gymId) throws WriterException, IOException {
    return generateAccessQrService.generateQrCode(memberId, gymId);
  }

  public List<MemberAccess> getAccesses(UUID memberId) {
    return membersRepository.getMemberAccessesByMemberId(memberId);
  }

  public byte[] getInvoiceReport(UUID memberId, UUID invoiceId) throws IOException {
    return generateInvoiceReportService.generate(1, invoiceId);
  }

}
