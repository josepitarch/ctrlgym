package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.MemberGuardianAuthorization;
import dev.jpitarch.ctrlgym.core.entities.MemberGuardianAuthorizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface MemberGuardianAuthorizationMapper {

  @Mapping(target = "member", ignore = true)
  MemberGuardianAuthorization toDomain(MemberGuardianAuthorizationEntity entity);

  @Mapping(target = "member", ignore = true)
  MemberGuardianAuthorizationEntity toEntity(MemberGuardianAuthorization domain);
}
