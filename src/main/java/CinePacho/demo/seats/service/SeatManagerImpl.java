package CinePacho.demo.seats.service;

import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SeatManagerImpl implements SeatManager {

    private final SeatRepository seatRepository;
    private final RoomManager roomManager;

    public SeatManagerImpl(SeatRepository seatRepository, RoomManager roomManager) {
        this.seatRepository = seatRepository;
        this.roomManager = roomManager;
    }


    @Override
    public void createSeat(int general, int preferential, UUID roomId) {

        // Crear sillas generales
        for (int i = 0; i < general; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i);
            seat.setType(SeatType.GENERAL);
            seat.setRoom(roomManager.getRoom(roomId));

            seatRepository.save(seat);
        }

        //Crear sillas preferenciales
        for (int i = 0; i < preferential; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i + general);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(roomManager.getRoom(roomId));

            seatRepository.save(seat);
        }

    }

}
