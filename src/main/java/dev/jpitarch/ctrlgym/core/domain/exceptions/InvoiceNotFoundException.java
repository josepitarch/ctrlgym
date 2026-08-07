package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class InvoiceNotFoundException extends RuntimeException {

  public InvoiceNotFoundException(String invoiceId) {
    super("Invoice with id %s does not exists".formatted(invoiceId));
  }

}
