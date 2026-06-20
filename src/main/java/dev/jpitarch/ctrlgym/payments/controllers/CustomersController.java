package dev.jpitarch.ctrlgym.payments.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/customers")
public class CustomersController {

  private final CustomerService customerService;

  @PostMapping("")
  public ResponseEntity<String> create() throws StripeException {
    //TODO
    //customerService.create(UUID.randomUUID());
    return ResponseEntity.noContent().build();
  }

}
