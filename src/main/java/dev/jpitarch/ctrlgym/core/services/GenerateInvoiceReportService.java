package dev.jpitarch.ctrlgym.core.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dev.jpitarch.ctrlgym.verifactu.service.VerifactuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInvoiceReportService {

  private final VerifactuService verifactuService;

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
          .parties { width: 100%; margin-top: 24px; }
          .parties td { vertical-align: top; width: 50%; padding-right: 12px; }
          .parties .title { font-size: 9pt; text-transform: uppercase; color: #888; letter-spacing: 1px; margin-bottom: 4px; }
          .parties .box { border: 1px solid #ddd; padding: 10px; }
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
              <div class="name">ACME Corporation S.L.</div>
              <div>Calle Mayor 123</div>
              <div>46001 Valencia, Espa&#241;a</div>
              <div>CIF: B12345678</div>
              <div>billing@acme.example</div>
            </td>
            <td class="meta">
              <h1>FACTURA</h1>
              <div><span class="label">N&#250;mero:</span> 2026-0001</div>
              <div><span class="label">Fecha emisi&#243;n:</span> 27/05/2026</div>
              <div><span class="label">Vencimiento:</span> 26/06/2026</div>
            </td>
          </tr>
        </table>
    
        <table class="parties">
          <tr>
            <td>
              <div class="title">Facturar a</div>
              <div class="box">
                <div><strong>Cliente Ejemplo S.A.</strong></div>
                <div>Avenida del Sol 45</div>
                <div>28013 Madrid, Espa&#241;a</div>
                <div>CIF: A87654321</div>
              </div>
            </td>
            <td>
              <div class="title">Enviar a</div>
              <div class="box">
                <div><strong>Cliente Ejemplo S.A.</strong></div>
                <div>Pol&#237;gono Industrial Norte, Nave 12</div>
                <div>28100 Alcobendas, Espa&#241;a</div>
              </div>
            </td>
          </tr>
        </table>
    
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
              <td>Servicio de consultor&#237;a t&#233;cnica</td>
              <td class="num">10</td>
              <td class="num">75,00 &#8364;</td>
              <td class="num">750,00 &#8364;</td>
            </tr>
            <tr>
              <td>Desarrollo de software a medida</td>
              <td class="num">40</td>
              <td class="num">60,00 &#8364;</td>
              <td class="num">2.400,00 &#8364;</td>
            </tr>
            <tr>
              <td>Soporte y mantenimiento mensual</td>
              <td class="num">1</td>
              <td class="num">350,00 &#8364;</td>
              <td class="num">350,00 &#8364;</td>
            </tr>
          </tbody>
        </table>
    
        <table class="totals">
          <tr>
            <td class="label">Subtotal</td>
            <td class="num">3.500,00 &#8364;</td>
          </tr>
          <tr>
            <td class="label">IVA (21%)</td>
            <td class="num">735,00 &#8364;</td>
          </tr>
          <tr class="grand">
            <td class="label">Total</td>
            <td class="num">4.235,00 &#8364;</td>
          </tr>
        </table>
    
        <div class="notes">
          <strong>Forma de pago:</strong> Transferencia bancaria a ES12 3456 7890 1234 5678 9012 en un plazo m&#225;ximo de 30 d&#237;as desde la fecha de emisi&#243;n.<br/>
          Gracias por su confianza.
        </div>
        <div class="footer">
          <img width="200px" height="200px" src="data:image/png;base64,{{QR_CODE_BASE64}}" />
          <div>Verifactu</div>
        </div>
      </body>
    </html>
    """;

  public byte[] generate(Integer gymId, UUID invoiceId) {
    var qrUrl = verifactuService.getStatus(gymId, invoiceId).getQr();
    try {

      var html = DEFAULT_HTML.replace("{{QR_CODE_BASE64}}", qrUrl);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();
        return os.toByteArray();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to generate PDF", e);
    }
  }
}
