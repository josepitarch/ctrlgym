package dev.jpitarch.ctrlgym.payments.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.payments.dtos.SetupIntentResponse;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping("/members/{memberId}/payment-method")
  public ResponseEntity<SetupIntentResponse> createSetupIntent(@PathVariable UUID memberId) throws StripeException {
    SetupIntentResponse response = customerService.createSetupIntent(memberId);

    return ResponseEntity.ok(response);
  }

  @PutMapping("/members/{memberId}/payment-method")
  public ResponseEntity<Void> updateSetupIntent(@PathVariable UUID memberId) throws StripeException {
    //TODO: revisar coEntity implementar esto
    return ResponseEntity.ok().build();
  }


}
