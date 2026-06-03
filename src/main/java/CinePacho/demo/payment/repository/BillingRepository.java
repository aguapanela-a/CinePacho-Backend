package CinePacho.demo.payment.repository;

import CinePacho.demo.payment.entities.BillingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillingRepository extends JpaRepository<BillingEntity, UUID> {

BillingEntity findByPayment_PaymentId(UUID paymentPaymentId);

// Busca las facturas asociadas al usuario (userId) a través de la relación Buyer -> User
List<BillingEntity> findAllByBuyer_User_UserIdOrderByCreatedAtDesc(UUID userId);

List<BillingEntity> findAllByBuyer_BuyerId(UUID buyerBuyerId);
}
