package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.entities.MembershipCancellationReasonTranslationEntity;
import dev.jpitarch.ctrlgym.core.entities.MembershipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = BaseMapper.class)
public interface MembershipMapper {

  @Mapping(target = "planId", source = "membershipPlanId")
  @Mapping(target = "datePeriod", expression = "java(new dev.jpitarch.ctrlgym.core.domain.DatePeriod(m.getStartDate(), m.getEndDate()))")
  Membership map(MembershipEntity m);

  @Mapping(target = "id", source = "cancellationReason.id")
  MembershipCancellationReason toDomain(MembershipCancellationReasonTranslationEntity translation);

}
