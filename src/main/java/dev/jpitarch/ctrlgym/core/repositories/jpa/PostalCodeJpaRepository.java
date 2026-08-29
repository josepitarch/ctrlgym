package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.PostalCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public interface PostalCodeJpaRepository extends JpaRepository<PostalCodeEntity, PostalCodeEntity.ID> {

  List<PostalCodeEntity> findAllByPostalCode(Integer postalCode);

  Optional<PostalCodeEntity> findByPostalCode(Integer postalCode);

  List<PostalCodeEntity> findAllByPostalCodeIn(List<Integer> postalCodes);

  default Map<Integer, PostalCodeEntity> findMapByPostalCodeIn(List<Integer> postalCodes) {
    return findAllByPostalCodeIn(postalCodes).stream()
      .collect(Collectors.toMap(
        PostalCodeEntity::getPostalCode,
        Function.identity(),
        (a, b) -> a)
      );
  }

}
