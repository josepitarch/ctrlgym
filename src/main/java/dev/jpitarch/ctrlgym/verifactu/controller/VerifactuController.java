package dev.jpitarch.ctrlgym.verifactu.controller;

import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceResponse;
import dev.jpitarch.ctrlgym.verifactu.dto.StatusResponse;
import dev.jpitarch.ctrlgym.verifactu.service.VerifactuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/verifactu")
@RequiredArgsConstructor
public class VerifactuController {

  private final VerifactuService verifactuService;

  @PostMapping("/create")
  public ResponseEntity<CreateInvoiceResponse> createFactura(
      @RequestBody CreateInvoiceRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

    CreateInvoiceResponse response;
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      response = verifactuService.createFacturaWithIdempotency(request, idempotencyKey);
    } else {
      response = verifactuService.createFactura(request);
    }

    return ResponseEntity.ok(response);
  }

  @GetMapping("/status")
  public ResponseEntity<StatusResponse> getStatus(@RequestParam String uuid) {
    StatusResponse response = verifactuService.getStatus(uuid);
    return ResponseEntity.ok(response);
  }
}