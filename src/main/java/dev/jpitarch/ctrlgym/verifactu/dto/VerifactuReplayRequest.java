package dev.jpitarch.ctrlgym.verifactu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record VerifactuReplayRequest(@JsonProperty("invoice_ids") List<String> invoiceIds) {
}
