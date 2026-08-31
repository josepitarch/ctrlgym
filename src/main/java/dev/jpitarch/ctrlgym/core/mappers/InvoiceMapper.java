package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.entities.InvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface InvoiceMapper {

  @Mapping(target = "name", ignore = true)
  @Mapping(target = "firstSurname", ignore = true)
  @Mapping(target = "secondSurname", ignore = true)
  @Mapping(target = "nif", ignore = true)
  Invoice map(InvoiceEntity mo);

  @Mapping(target = "verifactuId", ignore = true)
  @Mapping(target = "nextAttempt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "membershipId", ignore = true)
  @Mapping(target = "gymId", ignore = true)
  InvoiceEntity map(Invoice inv);

}
