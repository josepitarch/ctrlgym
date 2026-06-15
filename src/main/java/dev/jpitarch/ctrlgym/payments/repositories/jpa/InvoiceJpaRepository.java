package dev.jpitarch.ctrlgym.payments.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.WorkoutMO;
import dev.jpitarch.ctrlgym.payments.models.InvoiceMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceMO, Long> {
}
