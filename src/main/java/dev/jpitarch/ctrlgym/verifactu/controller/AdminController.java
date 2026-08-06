package dev.jpitarch.ctrlgym.verifactu.controller;

import dev.jpitarch.ctrlgym.verifactu.dto.VerifactuReplayRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.VerifactuReplayResponse;
import dev.jpitarch.ctrlgym.verifactu.service.VerifactuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

  private final VerifactuService verifactuService;

  @PostMapping("/verifactu/replay")
  public VerifactuReplayResponse replay(@RequestBody VerifactuReplayRequest request) {
    var succeeded = new ArrayList<String>();
    var failed = new LinkedHashMap<String, String>();

    for (var invoiceId : request.invoiceIds()) {
      try {
        verifactuService.processInvoice(invoiceId);
        succeeded.add(invoiceId);
      } catch (Exception e) {
        failed.put(invoiceId, e.getMessage());
      }
    }

    return new VerifactuReplayResponse(succeeded, failed);
  }

}
