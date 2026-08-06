package dev.jpitarch.ctrlgym.verifactu.dto;

import java.util.List;
import java.util.Map;

public record VerifactuReplayResponse(List<String> succeeded, Map<String, String> failed) {
}
