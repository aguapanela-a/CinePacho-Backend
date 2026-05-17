package CinePacho.demo.seats.service;

import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomProvider;
import CinePacho.demo.shared.auxiliaryClass.SeatGenerator;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SeatGeneratorImpl implements SeatGenerator {

    private final SeatRepository seatRepository;
    private final RoomProvider roomProvider;

    public SeatGeneratorImpl(SeatRepository seatRepository, RoomProvider roomProvider) {
        this.seatRepository = seatRepository;
        this.roomProvider = roomProvider;
    }


    @Override
    public void createSeat(int general, int preferential, UUID roomId) {

        // Crear sillas generales
        for (int i = 0; i < general; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i);
            seat.setType(SeatType.GENERAL);
            seat.setRoom(roomProvider.getRoom(roomId));

            seatRepository.save(seat);
        }

        //Crear sillas preferenciales
        for (int i = 0; i < preferential; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(general + 1 + i);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(roomProvider.getRoom(roomId));

            seatRepository.save(seat);
        }

    }
}
