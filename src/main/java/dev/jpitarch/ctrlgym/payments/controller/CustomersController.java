package dev.jpitarch.ctrlgym.payments.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import dev.jpitarch.ctrlgym.payments.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomersController {

  private final CustomerService customerService;

  @PostMapping("/")
  public ResponseEntity<String> create() throws StripeException {
    //TODO
    customerService.create(UUID.randomUUID());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) throws StripeException {
    Customer customer = customerService.retrieve(customerId);
    return ResponseEntity.ok(customer);
  }

}
