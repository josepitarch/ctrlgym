package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.TaxIdCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  public String create(Member.Id memberId) throws StripeException {
    Member member = membersRepository.getById(memberId);
    Integer gymId = member.getId().gymId();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    var params = CustomerCreateParams.builder()
      .setEmail(member.getEmail())
      .setName(member.getName())
      /*.addTaxIdData(CustomerCreateParams.TaxIdData.builder()
        .setType(CustomerCreateParams.TaxIdData.Type.ES_CIF)
        .setValue("45911747K")
        .build()
      )*/
      .setMetadata(Map.of(
        "gymId", gymId.toString()
      ))
      .build();

    log.info("Creating a customer with id {} of gym with id {}...", memberId, gymId);

    var customer = Customer.create(params, requestOptions);
    membersRepository.saveCustomerId(member.getId(), customer.getId());

    return customer.getId();
  }

}
