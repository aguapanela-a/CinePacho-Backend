package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface RoomManager {
     RoomEntity getRoom(UUID id);
     void createRoom(MultiplexEntity multiplex);
     void createRoom(MultiplexEntity multiplex, Integer numberRoom);
     List<UUID> getRoomIdsByMultiplexId(UUID multiplexId);
     Integer countByMultiplexId(UUID multiplexId);
}
