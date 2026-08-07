package dev.jpitarch.ctrlgym.core.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BaseMapper {
}
