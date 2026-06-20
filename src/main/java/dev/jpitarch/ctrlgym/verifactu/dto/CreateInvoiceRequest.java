package dev.jpitarch.ctrlgym.verifactu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

  @JsonProperty("serie")
  private String serie;

  @JsonProperty("numero")
  private String numero;

  @JsonProperty("fecha_expedicion")
  private String expeditionDate;

  @JsonProperty("tipo_factura")
  private String invoiceType;

  @JsonProperty("descripcion")
  private String description;

  @JsonProperty("lineas")
  private List<Line> lines;

  @JsonProperty("importe_total")
  private String totalAmount;

  @JsonProperty("fecha_operacion")
  private String operationDate;

  @JsonProperty("nif")
  private String nif;

  @JsonProperty("id_otro")
  private IdOtro otherId;

  @JsonProperty("nombre")
  private String name;

  @JsonProperty("validar_destinatario")
  private Boolean validateRecipient;

  @JsonProperty("tipo_rectificativa")
  private String correctiveType;

  @JsonProperty("importe_rectificativa")
  private ImporteRectificativa correctiveAmount;

  @JsonProperty("facturas_rectificadas")
  private List<FacturaRectificada> correctedInvoices;

  @JsonProperty("facturas_sustituidas")
  private List<FacturaSustituida> substitutedInvoices;

  @JsonProperty("incidencia")
  private String incident;

  @JsonProperty("especial")
  private Especial special;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Line {

    @JsonProperty("base_imponible")
    private String taxableBase;

    @JsonProperty("tipo_impositivo")
    private String taxRate;

    @JsonProperty("cuota_repercutida")
    private String repercussedQuota;

    @JsonProperty("impuesto")
    private String tax;

    @JsonProperty("calificacion_operacion")
    private String operationClassification;

    @JsonProperty("clave_regimen")
    private String regimeKey;

    @JsonProperty("operacion_exenta")
    private String exemptOperation;

    @JsonProperty("base_imponible_a_coste")
    private String taxableBaseAtCost;

    @JsonProperty("tipo_recargo_equivalencia")
    private String equivalenceSurchargeRate;

    @JsonProperty("cuota_recargo_equivalencia")
    private String equivalenceSurchargeQuota;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IdOtro {
    @JsonProperty("codigo_pais")
    private String countryCode;

    @JsonProperty("id_type")
    private String idType;

    @JsonProperty("id")
    private String id;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ImporteRectificativa {
    @JsonProperty("base_rectificada")
    private String correctedBase;

    @JsonProperty("cuota_rectificada")
    private String correctedQuota;

    @JsonProperty("cuota_recargo_rectificada")
    private String correctedEquivalenceSurcharge;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacturaRectificada {
    @JsonProperty("serie")
    private String serie;
    @JsonProperty("numero")
    private String numero;

    @JsonProperty("fecha_expedicion")
    private String expeditionDate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacturaSustituida {
    @JsonProperty("serie")
    private String serie;
    @JsonProperty("numero")
    private String numero;

    @JsonProperty("fecha_expedicion")
    private String expeditionDate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Especial {
    @JsonProperty("cupon")
    private String coupon;

    @JsonProperty("factura_simplificada_art_7273")
    private String simplifiedInvoiceArt7273;

    @JsonProperty("factura_sin_identif_destinatario_art_61d")
    private String invoiceWithoutRecipientIdArt61d;

    @JsonProperty("emitida_por_tercero_o_destinatario")
    private String issuedByThirdPartyOrRecipient;

    @JsonProperty("nombre_tercero")
    private String thirdPartyName;

    @JsonProperty("nif_tercero")
    private String thirdPartyNif;

    @JsonProperty("id_otro_tercero")
    private IdOtro thirdPartyOtherId;
  }
}
