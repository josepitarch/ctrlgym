package dev.jpitarch.ctrlgym.core.services;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.services.GenerateInvoiceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

  public Map<LocalDate, Boolean> getAttendanceSummary(Member.Id memberId, LocalDate from, LocalDate to) {
    OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
    OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

    var accesses = membersRepository.getMemberAccessesByMemberIdAndDateRange(memberId, fromDt, toDt);

    var accessedDates = accesses.stream()
      .map(access -> access.getTimestamp().toLocalDate())
      .collect(Collectors.toSet());

    return from.datesUntil(to.plusDays(1))
      .collect(Collectors.toMap(
        date -> date,
        accessedDates::contains
      ));
  }


}
