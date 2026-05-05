package CinePacho.demo.client.repository;

import CinePacho.demo.client.entitites.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface BuyerRepository extends JpaRepository<BuyerEntity, UUID> {
    BuyerEntity findByUser_Email(String userEmail);
}
