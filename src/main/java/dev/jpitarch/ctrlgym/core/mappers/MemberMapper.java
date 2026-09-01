package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.entities.MemberAccessEntity;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = BaseMapper.class)
public interface MemberMapper {

  @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGenderToDomain")
  @Mapping(target = "address.postalCode", source = "postalCode")
  @Mapping(target = "isActive", ignore = true)
  @Mapping(target = "iban", ignore = true)
  @Mapping(target = "avatarUrl", ignore = true)
  Member toDomain(UserEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "gymId", ignore = true)
  @Mapping(target = "avatarUrl", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "stripeCustomerId", ignore = true)
  @Mapping(target = "stripeSetupIntentId", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGenderToEntity")
  @Mapping(target = "postalCode", source = "address.postalCode")
  void updateEntity(Member member, @MappingTarget UserEntity entity);

  @Mapping(target = "branchId", source = "gymBranchId")
  @Mapping(target = "direction", source = "direction", qualifiedByName = "mapDirection")
  @Mapping(target = "timestamp", source = "createdAt")
  MemberAccess toDomain(MemberAccessEntity entity);

  @Named("mapGenderToDomain")
  default Gender mapGenderToDomain(String gender) {
    return switch (gender) {
      case "M" -> Gender.MALE;
      case "F" -> Gender.FEMALE;
      case null -> null;
      default -> throw new IllegalStateException("Unexpected value: " + gender);
    };
  }

  @Named("mapGenderToEntity")
  default String mapGenderToEntity(Gender gender) {
    return switch (gender) {
      case MALE -> "M";
      case FEMALE -> "F";
    };
  }

  @Named("mapDirection")
  default MemberAccess.Direction mapDirection(Integer direction) {
    return switch (direction) {
      case 0 -> MemberAccess.Direction.IN;
      case 1 -> MemberAccess.Direction.OUT;
      default -> throw new IllegalStateException("Unexpected value: " + direction);
    };
  }
}
