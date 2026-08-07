package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.models.InvoiceMO;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface InvoiceMapper {

  @Mapping(target = "name", ignore = true)
  @Mapping(target = "firstSurname", ignore = true)
  @Mapping(target = "secondSurname", ignore = true)
  @Mapping(target = "nif", ignore = true)
  Invoice map(InvoiceMO mo);

  @Mapping(target = "memberId", source = "memberId.memberId")
  @Mapping(target = "gymId", source = "memberId.gymId")
  @Mapping(target = "verifactuId", ignore = true)
  @Mapping(target = "nextAttempt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "membershipId", ignore = true)
  InvoiceMO map(Invoice inv);

}
