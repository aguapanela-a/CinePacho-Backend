package CinePacho.demo.points.repository;

import CinePacho.demo.points.entities.PointsConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PointsConfigRepository extends JpaRepository<PointsConfigEntity, UUID> {
    Optional<PointsConfigEntity> findTopByOrderByIdDesc();
}