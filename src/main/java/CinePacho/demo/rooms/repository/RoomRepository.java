package CinePacho.demo.rooms.repository;

import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.UUID;
 
@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
 
    boolean existsByMultiplexIdAndNumberRoom(UUID multiplexId, Integer numberRoom);
}
 