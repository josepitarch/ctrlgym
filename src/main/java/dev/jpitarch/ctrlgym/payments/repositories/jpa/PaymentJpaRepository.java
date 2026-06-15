package dev.jpitarch.ctrlgym.payments.repositories.jpa;

import dev.jpitarch.ctrlgym.payments.models.PaymentMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentMO, Long> {
}
