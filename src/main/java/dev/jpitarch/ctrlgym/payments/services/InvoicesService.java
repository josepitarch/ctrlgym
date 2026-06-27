package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.SetupIntentCreateParams;
import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.dto.SetupIntentResponse;
import dev.jpitarch.ctrlgym.payments.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoicesService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final CustomerService customerService;

  private final InvoiceRepository invoiceRepository;

  public SetupIntentResponse createSetupIntent(Member.Id memberId) throws StripeException {
    String accountId = gymsRepository.getStripeAccountId(memberId.gymId());
    String customerId = membersRepository.getStripeCustomerId(memberId).orElseThrow();

    var requestOptions = RequestOptions.builder()
      .setStripeAccount(accountId)
      .build();

    var params = SetupIntentCreateParams.builder()
      .setCustomer(customerId)
      .addPaymentMethodType("sepa_debit")
      .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION) // <- no requiere confirmación del usuario en ese momento. Se cobrará en el futuro
      .build();

    var setupIntent = SetupIntent.create(params, requestOptions);

    return new SetupIntentResponse(setupIntent.getId(), setupIntent.getClientSecret());
  }

  public Page<Invoice> getInvoices(Member.Id memberId, Pageable pageable) {
    return invoiceRepository.findByMemberId(memberId, pageable);
  }
}
