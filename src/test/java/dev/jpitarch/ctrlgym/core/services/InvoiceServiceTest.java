package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

  @InjectMocks
  InvoiceService invoiceService;

  @Mock
  InvoiceRepository invoiceRepository;

  @Mock
  MembersRepository membersRepository;

  private final UUID memberId = UUID.randomUUID();

  private final Invoice baseInvoice = Invoice.builder()
    .id("inv-001")
    .memberId(memberId)
    .series("A")
    .number("001")
    .issueAt(LocalDate.of(2025, 3, 15))
    .subtotal(new BigDecimal("100.00"))
    .tax(new BigDecimal("21.00"))
    .total(new BigDecimal("121.00"))
    .build();

  private final Member member = Member.builder()
    .id(memberId)
    .name("Juan")
    .firstSurname("García")
    .secondSurname("López")
    .nif("12345678A")
    .email("juan@example.com")
    .build();

  @Test
  @DisplayName("getInvoiceWithMemberData returns complete invoice with member data")
  void getInvoiceWithMemberData_returnsCompleteInvoice() {
    when(invoiceRepository.getInvoice("inv-001")).thenReturn(Optional.of(baseInvoice));
    when(membersRepository.getById(memberId)).thenReturn(member);

    Invoice result = invoiceService.getInvoiceWithMemberData("inv-001");

    assertThat(result.getId()).isEqualTo("inv-001");
    assertThat(result.getMemberId()).isEqualTo(memberId);
    assertThat(result.getName()).isEqualTo("Juan");
    assertThat(result.getFirstSurname()).isEqualTo("García");
    assertThat(result.getSecondSurname()).isEqualTo("López");
    assertThat(result.getNif()).isEqualTo("12345678A");
    assertThat(result.getSeries()).isEqualTo("A");
    assertThat(result.getNumber()).isEqualTo("001");
    assertThat(result.getIssueAt()).isEqualTo(LocalDate.of(2025, 3, 15));
    assertThat(result.getSubtotal()).isEqualByComparingTo("100.00");
    assertThat(result.getTax()).isEqualByComparingTo("21.00");
    assertThat(result.getTotal()).isEqualByComparingTo("121.00");
    assertThat(result.getFullName()).isEqualTo("Juan García López");
  }
}
