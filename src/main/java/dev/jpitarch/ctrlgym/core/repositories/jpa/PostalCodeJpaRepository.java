package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.PostalCodeMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public interface PostalCodeJpaRepository extends JpaRepository<PostalCodeMO, PostalCodeMO.ID> {

  List<PostalCodeMO> findAllByPostalCode(Integer postalCode);

  Optional<PostalCodeMO> findByPostalCode(Integer postalCode);

  List<PostalCodeMO> findAllByPostalCodeIn(List<Integer> postalCodes);

  default Map<Integer, PostalCodeMO> findMapByPostalCodeIn(List<Integer> postalCodes) {
    return findAllByPostalCodeIn(postalCodes).stream()
      .collect(Collectors.toMap(
        PostalCodeMO::getPostalCode,
        Function.identity(),
        (a, b) -> a)
      );
  }

}
