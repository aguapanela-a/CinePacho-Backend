package CinePacho.demo.auth.entities.customers.repository;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuyerRepository extends JpaRepository<BuyerEntity, UUID> {
    BuyerEntity findByUser_Email(String userEmail);
}
