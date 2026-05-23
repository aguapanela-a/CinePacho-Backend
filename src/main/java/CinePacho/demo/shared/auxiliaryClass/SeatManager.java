package CinePacho.demo.shared.auxiliaryClass;
import CinePacho.demo.rooms.entities.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface SeatManager {
    void createSeat(int general, int preferential,  RoomEntity room);
}
