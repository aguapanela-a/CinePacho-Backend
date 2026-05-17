package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface RoomProvider {
    public RoomEntity getRoom(UUID id);
}
