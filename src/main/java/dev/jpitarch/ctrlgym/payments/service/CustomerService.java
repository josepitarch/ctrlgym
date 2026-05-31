package dev.jpitarch.ctrlgym.payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final GymsRepository gymsRepository;

  public String create(String email, String name) throws StripeException {
    Integer gymId = 1;
    var params = CustomerCreateParams.builder()
      .setEmail(email)
      .setName(name)
      .setMetadata(Map.of(
        "gymId", gymId.toString()
      ))
      .build();

    RequestOptions requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    Customer customer = Customer.create(params, requestOptions);
    return customer.getId();
  }

  public Customer retrieve(String customerId) throws StripeException {
    Integer gymId = 1;
    var requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    return Customer.retrieve(customerId, requestOptions);
  }
}
