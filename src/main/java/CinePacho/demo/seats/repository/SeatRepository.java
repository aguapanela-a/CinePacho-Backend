package CinePacho.demo.seats.repository;

import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
 
@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
 
    List<SeatEntity> findByRoomId(UUID roomId);

    List<SeatEntity> findByTypeAndRoomId(SeatType type, UUID roomId, Sort sort, Limit limit);

    Integer countByRoomIdAndType(UUID roomId, SeatType type);

    boolean existsByRoomIdAndSeatNumber(UUID roomId, Integer seatNumber);

    Integer countByRoomId(UUID roomId); //Cuanta todas las sillas asociadas al UUID de una sala

    List<SeatEntity> findByStatus(SeatStatus status);

    // Carga sillas con sala y multiplex para evitar lazy loading en compras
    @Query("select s from SeatEntity s join fetch s.room r join fetch r.multiplex where s.id in :ids")
    List<SeatEntity> findAllByIdWithRoomAndMultiplex(@Param("ids") List<UUID> ids);
}
