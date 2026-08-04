package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;

  private final MembersRepository membersRepository;

  public Invoice getInvoiceWithMemberData(String invoiceId) {
    Invoice invoice = invoiceRepository.getInvoice(invoiceId).orElseThrow();
    Member member = membersRepository.getById(invoice.getMemberId());

    return Invoice.builder()
      .id(invoice.getId())
      .memberId(invoice.getMemberId())
      .name(member.getName())
      .firstSurname(member.getFirstSurname())
      .secondSurname(member.getSecondSurname())
      .nif(member.getNif())
      .series(invoice.getSeries())
      .number(invoice.getNumber())
      .issueAt(invoice.getIssueAt())
      .subtotal(invoice.getSubtotal())
      .tax(invoice.getTax())
      .total(invoice.getTotal())
      .build();
  }
}
