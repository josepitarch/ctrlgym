package dev.jpitarch.ctrlgym.payments.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.payments.dto.InvoiceSummary;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import dev.jpitarch.ctrlgym.payments.services.InvoicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class InvoicesController {

  private final InvoicesService invoicesService;

  @PostMapping("/members/{memberId}/payment-methods")
  public ResponseEntity<SetupIntentResponse> createIntent(@PathVariable UUID memberId, @RequestParam Integer gymId) throws StripeException {
    SetupIntentResponse response = invoicesService.createSetupIntent(Member.Id.of(memberId, gymId));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/members/{memberId}/invoices")
  public Page<InvoiceSummary> getInvoices(@PathVariable UUID memberId, @RequestParam Integer gymId, Pageable pageable) {
    return invoicesService.getInvoices(Member.Id.of(memberId, gymId), pageable)
      .map(invoice -> new InvoiceSummary(
        invoice.getId(),
        invoice.getIssueAt(),
        null,
        invoice.getTotal()
      ));
  }

  @GetMapping(value = "/members/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable UUID memberId, @PathVariable String invoiceId, @RequestParam Integer gymId) throws IOException {
    byte[] pdfReport = invoicesService.getInvoiceReport(Member.Id.of(memberId, gymId), invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }


}
