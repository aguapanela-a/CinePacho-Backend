package CinePacho.demo.seats.service;

import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.stereotype.Component;


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
            seat.setRoom(room);

            seatRepository.save(seat);
        }

        //Crear sillas preferenciales
        for (int i = 0; i < preferential; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i + general);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(room);

            seatRepository.save(seat);
        }

    }

}
