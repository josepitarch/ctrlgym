package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Shift;
import dev.jpitarch.ctrlgym.core.domain.ShiftSeries;
import dev.jpitarch.ctrlgym.core.entities.ShiftEntity;
import dev.jpitarch.ctrlgym.core.entities.ShiftSeriesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface ShiftMapper {

  ShiftSeries map(ShiftSeriesEntity mo);

  @Mapping(target = "createdAt", ignore = true)
  ShiftSeriesEntity map(ShiftSeries series);

  Shift map(ShiftEntity mo);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "series", ignore = true)
  ShiftEntity map(Shift shift);

}
