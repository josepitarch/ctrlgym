package dev.jpitarch.ctrlgym.payments.repositories.jpa;

import dev.jpitarch.ctrlgym.payments.models.InvoiceMO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceMO, String> {

  Page<InvoiceMO> findByMemberIdAndGymId(UUID memberId, Integer gymId, Pageable pageable);

}
