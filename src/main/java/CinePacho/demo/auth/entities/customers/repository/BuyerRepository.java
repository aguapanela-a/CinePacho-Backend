package CinePacho.demo.auth.entities.customers.repository;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BuyerRepository extends JpaRepository<BuyerEntity, UUID> {
    BuyerEntity findByBuyerId(UUID buyerId);
    Optional<BuyerEntity> getBuyerByEmail(String email);
}
