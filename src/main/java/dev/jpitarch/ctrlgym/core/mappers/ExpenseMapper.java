package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Expense;
import dev.jpitarch.ctrlgym.core.entities.ExpenseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(config = BaseMapper.class)
public interface ExpenseMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "amount", source = "amount", qualifiedByName = "doubleToBigDecimal")
  @Mapping(target = "estimatedAmount", source = "estimatedAmount", qualifiedByName = "doubleToBigDecimal")
  @Mapping(target = "source", source = "source", qualifiedByName = "sourceToString")
  @Mapping(target = "active", source = "active", defaultValue = "true")
  ExpenseEntity toEntity(Expense expense);

  @Named("doubleToBigDecimal")
  default BigDecimal doubleToBigDecimal(Double value) {
    return value != null ? BigDecimal.valueOf(value) : null;
  }

  @Named("sourceToString")
  default String sourceToString(Expense.Source source) {
    return source != null ? source.name() : "MANUAL";
  }
}
