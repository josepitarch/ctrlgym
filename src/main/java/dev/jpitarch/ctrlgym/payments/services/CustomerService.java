package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.TaxIdCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.dto.CreateCustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public String create(CreateCustomerRequest request) throws StripeException {
    Member member = membersRepository.getById(request.memberId());
    Integer gymId = member.getId().gymId();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    var params = CustomerCreateParams.builder()
      .setName(request.getFullName())
      .setEmail(member.getEmail())
      /*.addTaxIdData(CustomerCreateParams.TaxIdData.builder()
        .setType(CustomerCreateParams.TaxIdData.Type.ES_CIF)
        .setValue("45911747K")
        .build()
      )*/
      .setAddress(
        CustomerCreateParams.Address.builder()
          .setLine1(request.address())
          .setCity(request.city())
          .setPostalCode(request.postalCode().toString())
          .setCountry("ES")
          .build()
      )
      .setMetadata(Map.of(
        "nif", request.nif(),
        "gymId", gymId.toString()
      ))
      .build();

    log.info("Creating a customer with memberId {}...", request.memberId());

    var customer = Customer.create(params, requestOptions);
    membersRepository.saveCustomerId(member.getId(), customer.getId());

    return customer.getId();
  }

}
