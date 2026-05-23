package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface RoomManager {
     RoomEntity getRoom(UUID id);
     void createRoom(MultiplexEntity multiplex);
}
