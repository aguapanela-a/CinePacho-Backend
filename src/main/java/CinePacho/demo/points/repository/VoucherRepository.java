package CinePacho.demo.points.repository;

import CinePacho.demo.points.entities.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<VoucherEntity, UUID> {
    Optional<VoucherEntity> findByCode(String code);
}