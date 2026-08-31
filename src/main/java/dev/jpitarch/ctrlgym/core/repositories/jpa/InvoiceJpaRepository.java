package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, String> {

  Page<InvoiceEntity> findByMemberId(UUID memberId, Pageable pageable);

  @Query("SELECT i.verifactuId FROM InvoiceEntity i WHERE i.id = :invoiceId")
  Optional<UUID> getVerifactuId(String invoiceId);

}
