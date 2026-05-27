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

  private String serie;

  private String numero;

  private String fecha_expedicion;

  private String tipo_factura;

  private String descripcion;

  private List<LineaFactura> lineas;

  @JsonProperty("importe_total")
  private String importeTotal;

  @JsonProperty("fecha_operacion")
  private String fechaOperacion;

  private String nif;

  @JsonProperty("id_otro")
  private IdOtro idOtro;

  private String nombre;

  @JsonProperty("validar_destinatario")
  private Boolean validarDestinatario;

  @JsonProperty("tipo_rectificativa")
  private String tipoRectificativa;

  @JsonProperty("importe_rectificativa")
  private ImporteRectificativa importeRectificativa;

  @JsonProperty("facturas_rectificadas")
  private List<FacturaRectificada> facturasRectificadas;

  @JsonProperty("facturas_sustituidas")
  private List<FacturaSustituida> facturasSustituidas;

  private String incidencia;

  private Especial especial;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LineaFactura {

    @JsonProperty("base_imponible")
    private String baseImponible;

    @JsonProperty("tipo_impositivo")
    private String tipoImpositivo;

    @JsonProperty("cuota_repercutida")
    private String cuotaRepercutida;

    private String impuesto;

    @JsonProperty("calificacion_operacion")
    private String calificacionOperacion;

    @JsonProperty("clave_regimen")
    private String claveRegimen;

    @JsonProperty("operacion_exenta")
    private String operacionExenta;

    @JsonProperty("base_imponible_a_coste")
    private String baseImponibleACoste;

    @JsonProperty("tipo_recargo_equivalencia")
    private String tipoRecargoEquivalencia;

    @JsonProperty("cuota_recargo_equivalencia")
    private String cuotaRecargoEquivalencia;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IdOtro {
    @JsonProperty("codigo_pais")
    private String codigoPais;

    @JsonProperty("id_type")
    private String idType;

    private String id;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ImporteRectificativa {
    @JsonProperty("base_rectificada")
    private String baseRectificada;

    @JsonProperty("cuota_rectificada")
    private String cuotaRectificada;

    @JsonProperty("cuota_recargo_rectificada")
    private String cuotaRecargoRectificada;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacturaRectificada {
    private String serie;
    private String numero;

    @JsonProperty("fecha_expedicion")
    private String fechaExpedicion;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacturaSustituida {
    private String serie;
    private String numero;

    @JsonProperty("fecha_expedicion")
    private String fechaExpedicion;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Especial {
    private String cupon;

    @JsonProperty("factura_simplificada_art_7273")
    private String facturaSimplificadaArt7273;

    @JsonProperty("factura_sin_identif_destinatario_art_61d")
    private String facturaSinIdentifDestinatarioArt61d;

    @JsonProperty("emitida_por_tercero_o_destinatario")
    private String emitidaPorTerceroODestinatario;

    @JsonProperty("nombre_tercero")
    private String nombreTercero;

    @JsonProperty("nif_tercero")
    private String nifTercero;

    @JsonProperty("id_otro_tercero")
    private IdOtro idOtroTercero;
  }
}
