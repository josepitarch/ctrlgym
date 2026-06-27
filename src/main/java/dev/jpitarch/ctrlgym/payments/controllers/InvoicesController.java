package dev.jpitarch.ctrlgym.payments.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import dev.jpitarch.ctrlgym.payments.services.InvoicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public Page<Invoice> getInvoices(@PathVariable UUID memberId, @RequestParam Integer gymId, Pageable pageable) {
    return invoicesService.getInvoices(Member.Id.of(memberId, gymId), pageable);
  }

}
