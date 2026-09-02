package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.entities.MembershipPlanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface MembershipPlanMapper {

  MembershipPlan map(MembershipPlanEntity entity);

  @Mapping(target = "gymId", ignore = true)
  @Mapping(target = "stripePriceId", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  MembershipPlanEntity map(MembershipPlan plan);
}
