package br.com.saga_orquestrado.payment.core.repository;

import br.com.saga_orquestrado.payment.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
