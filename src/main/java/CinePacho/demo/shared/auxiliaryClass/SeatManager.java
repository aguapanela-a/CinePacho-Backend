package CinePacho.demo.shared.auxiliaryClass;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface SeatManager {
    void createSeat(int general, int preferential,  RoomEntity room);
    List<SeatEntity> findByStatus(SeatStatus status);
    SeatEntity save(SeatEntity seat);
    Integer countByRoomIdAndType(UUID roomId, SeatType type); // Conteo de sillas por tipo para salas
    Integer countByRoomId(UUID roomId); // Conteo total de sillas por sala
    List<SeatEntity> findAllByIdWithRoomAndMultiplex(List<UUID> ids); // Carga sillas con sala y multiplex
}
