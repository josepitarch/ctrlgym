package dev.jpitarch.ctrlgym.core.services;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

  private final ApplicationEventPublisher publisher;

  public void create(Member member) {
    if (!membersRepository.exists(member.getId())) {
      throw new RuntimeException("Member with id %s does not exist".formatted(member.getId()));
    }
    membersRepository.save(member);
    publisher.publishEvent(member);
  }

  public Member getMember(Member.Id memberId) {
    return membersRepository.getById(memberId);
  }


  public byte[] generateQrCode(Member.Id memberId) throws WriterException, IOException {
    return generateAccessQrService.generateQrCode(memberId);
  }

  public List<MemberAccess> getAccesses(Member.Id memberId) {
    return membersRepository.getMemberAccessesByMemberId(memberId);
  }

  public byte[] getInvoiceReport(Member.Id memberId, UUID invoiceId) throws IOException {
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }

}
