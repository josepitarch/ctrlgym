package dev.jpitarch.ctrlgym.payments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.net.RequestOptions;
import dev.jpitarch.ctrlgym.core.StripeBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("stripeInvoiceService")
@RequiredArgsConstructor
public class InvoiceService {

  private final StripeBridge stripeBridge;

  public Invoice retrieve(String id, Integer gymId) throws StripeException {
    String stripeAccountId = stripeBridge.getStripeAccountId(gymId);

    var options = RequestOptions.builder()
      .setStripeAccount(stripeAccountId)
      .build();
    return Invoice.retrieve(id, options);
  }

}
