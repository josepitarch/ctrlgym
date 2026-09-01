package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import dev.jpitarch.ctrlgym.core.entities.LegalDocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalDocumentVersionJpaRepository extends JpaRepository<LegalDocumentVersionEntity, UUID> {

  @Query("SELECT d FROM LegalDocumentVersionEntity d WHERE d.gymId = :gymId AND d.type = :type AND d.active IS true")
  Optional<LegalDocumentVersionEntity> findActiveByGymIdAndType(Integer gymId, LegalDocumentType type);

  @Query("SELECT d FROM LegalDocumentVersionEntity d WHERE d.gymId = :gymId AND d.active IS true")
  List<LegalDocumentVersionEntity> findAllActiveByGymId(Integer gymId);

}
