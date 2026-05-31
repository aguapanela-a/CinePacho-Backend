package CinePacho.demo.seats.repository;

import CinePacho.demo.seats.entities.SeatScreeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatScreeningRepository extends JpaRepository<SeatScreeningEntity, UUID> {
    Optional<SeatScreeningEntity> findBySeat_IdAndScreening_Id(UUID seatId, UUID screeningId);
    List<SeatScreeningEntity> findByScreening_Id(UUID screeningId);
    void deleteByScreening_Id(UUID screeningId);
    List<SeatScreeningEntity> findByStatus(CinePacho.demo.seats.enumeration.SeatStatus status);
}
