package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  public String create(UUID memberId) throws StripeException {
    Member member = membersRepository.getById(memberId);
    Integer gymId = member.getGymId();

    var params = CustomerCreateParams.builder()
      .setEmail(member.getEmail())
      .setName(member.getName())
      .setMetadata(Map.of(
        "gymId", gymId.toString()
      ))
      .build();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    var customer = Customer.create(params, requestOptions);
    membersRepository.saveCustomerId(member.getId(), customer.getId());

    return customer.getId();
  }

}
