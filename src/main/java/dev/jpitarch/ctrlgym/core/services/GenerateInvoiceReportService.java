package dev.jpitarch.ctrlgym.core.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.entities.GymEntity;
import dev.jpitarch.ctrlgym.core.entities.PostalCodeEntity;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.PostalCodeJpaRepository;
import dev.jpitarch.ctrlgym.verifactu.dto.StatusResponse;
import dev.jpitarch.ctrlgym.verifactu.service.VerifactuService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoiceReportService {

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final InvoiceService invoiceService;

  private final VerifactuService verifactuService;

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  private static final String DEFAULT_HTML = """
    <?xml version="1.0" encoding="UTF-8"?>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>Factura</title>
        <style>
          @page { size: A4; margin: 2cm; }
          body { font-family: Helvetica, Arial, sans-serif; font-size: 10pt; color: #333; }
          h1 { margin: 0; font-size: 28pt; color: #1a4f8a; letter-spacing: 2px; }
          .header { width: 100%; border-bottom: 2px solid #1a4f8a; padding-bottom: 12px; }
          .header td { vertical-align: top; }
          .company { font-size: 11pt; }
          .company .name { font-weight: bold; font-size: 13pt; color: #1a4f8a; }
          .meta { text-align: right; font-size: 10pt; }
          .meta .label { color: #888; }
          .client { width: 50%; margin-top: 24px; }
          .client .title { font-size: 9pt; text-transform: uppercase; color: #888; letter-spacing: 1px; margin-bottom: 4px; }
          .client .box { border: 1px solid #ddd; padding: 10px; }
          .items { width: 100%; border-collapse: collapse; margin-top: 24px; }
          .items th { background: #1a4f8a; color: #fff; text-align: left; padding: 8px; font-size: 9pt; text-transform: uppercase; }
          .items td { padding: 8px; border-bottom: 1px solid #eee; }
          .items .num { text-align: right; }
          .totals { width: 40%; margin-top: 16px; margin-left: 60%; border-collapse: collapse; }
          .totals td { padding: 6px 8px; }
          .totals .label { color: #555; }
          .totals .num { text-align: right; }
          .totals .grand td { border-top: 2px solid #1a4f8a; font-weight: bold; font-size: 12pt; color: #1a4f8a; }
          .notes { margin-top: 32px; font-size: 9pt; color: #666; border-top: 1px solid #eee; padding-top: 12px; }
          .footer { position: fixed; bottom: 1.2cm; right: 1.2cm; text-align: center; display: flex; flex-direction: column; align-items: flex-end; }
            .footer img { width: 50px; height: 50px; }
            .footer .label { font-size: 7pt; color: #666; margin-top: 2px; }
        </style>
      </head>
      <body>
        <table class="header">
          <tr>
            <td class="company">
              <div class="name">{{COMPANY_NAME}}</div>
              <div>{{COMPANY_STREET}}</div>
              <div>{{COMPANY_POSTAL_CODE}} {{COMPANY_LOCALITY}}</div>
              <div>CIF: {{COMPANY_CIF}}</div>
            </td>
            <td class="meta">
              <h1>FACTURA</h1>
              <div><span class="label">N&#250;mero:</span> {{INVOICE_NUMBER}}</div>
              <div><span class="label">Serie:</span> {{INVOICE_SERIES}}</div>
              <div><span class="label">Fecha emisi&#243;n:</span> {{INVOICE_ISSUE_AT}}</div>
            </td>
          </tr>
        </table>

        <div class="client">
          <div class="title">DATOS DEL CLIENTE</div>
          <div class="box">
            <div><strong>{{CLIENT_NAME}}</strong></div>
            <div>{{CLIENT_STREET}}</div>
            <div>{{CLIENT_POSTAL_CODE}} {{CLIENT_CITY}}</div>
            <div>NIF: {{CLIENT_NIF}}</div>
          </div>
        </div>

        <table class="items">
          <thead>
            <tr>
              <th>Descripci&#243;n</th>
              <th class="num">Cantidad</th>
              <th class="num">Precio</th>
              <th class="num">Importe</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ITEM_DESCRIPTION}}</td>
              <td class="num">1</td>
              <td class="num">{{ITEM_PRICE}} &#8364;</td>
              <td class="num">{{ITEM_AMOUNT}} &#8364;</td>
            </tr>
          </tbody>
        </table>

        <table class="totals">
          <tr>
            <td class="label">Subtotal</td>
            <td class="num">{{INVOICE_SUBTOTAL}} &#8364;</td>
          </tr>
          <tr>
            <td class="label">IVA (21%)</td>
            <td class="num">{{INVOICE_TAX}} &#8364;</td>
          </tr>
          <tr class="grand">
            <td class="label">Total</td>
            <td class="num">{{INVOICE_TOTAL}} &#8364;</td>
          </tr>
        </table>

        <div class="footer">
          <img width="200px" height="200px" src="data:image/png;base64,{{QR_CODE_BASE64}}" />
          <div>Veri*Factu</div>
        </div>
      </body>
    </html>
    """;

  @PostConstruct
  public void init() {
    XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, Level.OFF));
  }

  public byte[] generate(Member.Id memberId, String invoiceId) throws IOException {
    GymEntity gym = gymsRepository.getById(memberId.gymId());
    Member member = membersRepository.getById(memberId);
    Invoice invoice = invoiceService.getInvoiceWithMemberData(invoiceId);
    StatusResponse qrUrl = verifactuService.getStatus(memberId.gymId(), invoiceId);

    var decimalFormat = new DecimalFormat("#,##0.00");
    var dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    try (var os = new ByteArrayOutputStream()) {
      var html = DEFAULT_HTML
        .replace("{{COMPANY_NAME}}", gym.getName())
        .replace("{{COMPANY_STREET}}", gym.getStreet())
        .replace("{{COMPANY_POSTAL_CODE}}", String.valueOf(gym.getPostalCode()))
        .replace("{{COMPANY_LOCALITY}}", gym.getLocality())
        .replace("{{COMPANY_CIF}}", gym.getCif())
        .replace("{{CLIENT_NAME}}", member.getFullName())
        .replace("{{CLIENT_POSTAL_CODE}}", String.valueOf(member.getAddress().getPostalCode()))
        .replace("{{CLIENT_CITY}}", postalCodeJpaRepository.findByPostalCode(member.getAddress().getPostalCode()).map(PostalCodeEntity::getCity).orElse(""))
        .replace("{{CLIENT_NIF}}", member.getNif())
        .replace("{{INVOICE_SERIES}}", invoice.getSeries())
        .replace("{{INVOICE_NUMBER}}", invoice.getNumber())
        .replace("{{INVOICE_ISSUE_AT}}", invoice.getIssueAt().format(dateFormatter))
        .replace("{{ITEM_DESCRIPTION}}", member.getFullName())
        .replace("{{ITEM_PRICE}}", decimalFormat.format(invoice.getSubtotal()))
        .replace("{{ITEM_AMOUNT}}", decimalFormat.format(invoice.getSubtotal()))
        .replace("{{INVOICE_SUBTOTAL}}", decimalFormat.format(invoice.getSubtotal()))
        .replace("{{INVOICE_TAX}}", decimalFormat.format(invoice.getTax()))
        .replace("{{INVOICE_TOTAL}}", decimalFormat.format(invoice.getTotal()))
        .replace("{{QR_CODE_BASE64}}", qrUrl != null ? qrUrl.getQr() : "");
      var builder = new PdfRendererBuilder();
      builder.withHtmlContent(html, null);
      builder.toStream(os);
      builder.run();
      return os.toByteArray();
    }

  }

}
