package CinePacho.demo.seats.service;

import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SeatManagerImpl implements SeatManager {

    private final SeatRepository seatRepository;

    public SeatManagerImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    @Override
    public void createSeat(int general, int preferential, RoomEntity room) {

        // Crear sillas generales
        for (int i = 0; i < general; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i);
            seat.setType(SeatType.GENERAL);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setRoom(room);

            seatRepository.save(seat);
        }

        //Crear sillas preferenciales
        for (int i = 0; i < preferential; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i + general);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(room);

            seatRepository.save(seat);
        }

    }

    @Override
    public List<SeatEntity> findByStatus(SeatStatus status) {
        return seatRepository.findByStatus(status);
    }

    @Override
    public SeatEntity save(SeatEntity seat) {
        return seatRepository.save(seat);
    }

    @Override
    public Integer countByRoomIdAndType(UUID roomId, SeatType type) {
        // Encapsula el acceso al repositorio de sillas para otros módulos
        return seatRepository.countByRoomIdAndType(roomId, type);
    }

    @Override
    public Integer countByRoomId(UUID roomId) {
        // Encapsula el acceso al repositorio de sillas para otros módulos
        return seatRepository.countByRoomId(roomId);
    }

    @Override
    public List<SeatEntity> findAllByIdWithRoomAndMultiplex(List<UUID> ids) {
        // Evita el LazyLoading en validaciones de compra
        return seatRepository.findAllByIdWithRoomAndMultiplex(ids);
    }

    @Override
    public void updateSeatStatus(UUID seatId, SeatStatus status) {
        SeatEntity seat = seatRepository.getReferenceById(seatId);
        seatRepository.save(seat);
    }

}
