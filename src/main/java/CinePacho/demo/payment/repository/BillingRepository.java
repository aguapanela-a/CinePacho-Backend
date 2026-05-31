package CinePacho.demo.payment.repository;

import CinePacho.demo.payment.entities.BillingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BillingRepository extends JpaRepository<BillingEntity, UUID> {

    BillingEntity findByPayment_PaymentId(UUID paymentPaymentId);
}
