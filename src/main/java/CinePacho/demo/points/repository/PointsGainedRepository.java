package CinePacho.demo.points.repository;

import CinePacho.demo.points.entities.PointsGainedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointsGainedRepository extends JpaRepository<PointsGainedEntity, UUID> {
    List<PointsGainedEntity> findAllByBuyer_BuyerIdOrderByCreatedAtDesc(UUID buyerId);
}