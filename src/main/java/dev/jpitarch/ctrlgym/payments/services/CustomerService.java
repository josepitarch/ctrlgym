package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public String create(Member member) throws StripeException {
    Integer gymId = member.getId().gymId();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(gymsRepository.getStripeAccountId(gymId))
      .build();

    var params = CustomerCreateParams.builder()
      .setName(member.getFullName())
      .setEmail(member.getEmail())
      /*.addTaxIdData(CustomerCreateParams.TaxIdData.builder()
        .setType(CustomerCreateParams.TaxIdData.Type.ES_CIF)
        .setValue("45911747K")
        .build()
      )*/
      .setAddress(
        CustomerCreateParams.Address.builder()
          .setLine1(member.getAddress().getStreet())
          .setCity(member.getAddress().getCity())
          .setPostalCode(member.getAddress().getPostalCode().toString())
          .setCountry("ES")
          .build()
      )
      .setMetadata(Map.of(
        "nif", member.getNif(),
        "gymId", gymId.toString()
      ))
      .build();

    log.info("Creating a customer with memberId {}...", member.getId());

    var customer = Customer.create(params, requestOptions);
    membersRepository.saveCustomerId(member.getId(), customer.getId());

    return customer.getId();
  }

}
