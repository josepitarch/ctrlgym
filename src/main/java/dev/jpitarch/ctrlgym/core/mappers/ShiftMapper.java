package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Shift;
import dev.jpitarch.ctrlgym.core.domain.ShiftSeries;
import dev.jpitarch.ctrlgym.core.models.ShiftMO;
import dev.jpitarch.ctrlgym.core.models.ShiftSeriesMO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface ShiftMapper {

  ShiftSeries map(ShiftSeriesMO mo);

  @Mapping(target = "createdAt", ignore = true)
  ShiftSeriesMO map(ShiftSeries series);

  Shift map(ShiftMO mo);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "series", ignore = true)
  ShiftMO map(Shift shift);

}
