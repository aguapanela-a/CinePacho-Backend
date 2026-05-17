package CinePacho.demo.seats.repository;

import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;
 
@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
 
    List<SeatEntity> findByRoomId(UUID roomId);

    List<SeatEntity> findByTypeAndRoomId(SeatType type, UUID roomId, Sort sort, Limit limit);

    long countByRoomIdAndType(UUID roomId, SeatType type);

    boolean existsByRoomIdAndSeatNumber(UUID roomId, Integer seatNumber);

    long countByRoomId(UUID roomId); //Cuanta todas las sillas asociadas al UUID de una sala
}