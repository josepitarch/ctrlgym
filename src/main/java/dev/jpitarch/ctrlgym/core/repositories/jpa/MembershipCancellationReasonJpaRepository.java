package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MembershipCancellationReasonTranslationMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipCancellationReasonJpaRepository extends JpaRepository<MembershipCancellationReasonTranslationMO, MembershipCancellationReasonTranslationMO.ID> {

  @Query("""
    SELECT t FROM MembershipCancellationReasonTranslationMO t
    JOIN FETCH t.cancellationReason r
    WHERE t.languageCode = :languageCode AND r.active = true
    ORDER BY r.sortOrder
    """)
  List<MembershipCancellationReasonTranslationMO> findByLanguageCode(@Param("languageCode") String languageCode);
}
