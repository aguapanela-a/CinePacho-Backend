package CinePacho.demo.shared.auxiliaryClass;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface SeatManager {
    void createSeat(int general, int preferential,  RoomEntity room);
    List<SeatEntity> findByStatus(SeatStatus status);
    SeatEntity save(SeatEntity seat);
}
