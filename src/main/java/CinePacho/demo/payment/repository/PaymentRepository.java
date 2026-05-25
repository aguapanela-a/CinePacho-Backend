package CinePacho.demo.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import CinePacho.demo.payment.entities.PaymentEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    
}
