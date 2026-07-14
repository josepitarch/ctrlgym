package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.PostalCodeMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostalCodeJpaRepository extends JpaRepository<PostalCodeMO, PostalCodeMO.ID> {

  Optional<PostalCodeMO> findByPostalCode(String postalCode);

}
