package CinePacho.demo.rooms.repository;

import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
 
@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
 
    boolean existsByMultiplex_Id(UUID multiplexId);

    List<RoomEntity> findByMultiplex_Id(UUID multiplexId);

    RoomEntity findTopByMultiplex_IdOrderByCreatedAtDesc(UUID multiplexId);

    int countByMultiplex_Id(UUID multiplexId);

    List<UUID> getAllByMultiplex_Id(UUID multiplexId);
}
 