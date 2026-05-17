package CinePacho.demo.seats.service;

import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomProvider;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import jakarta.transaction.Transactional;
import lombok.ToString;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

@Component
public class SeatManagerImpl implements SeatManager {

    private final SeatRepository seatRepository;
    private final RoomProvider roomProvider;

    public SeatManagerImpl(SeatRepository seatRepository, RoomProvider roomProvider) {
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

            seat.setSeatNumber(i + general);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(roomProvider.getRoom(roomId));

            seatRepository.save(seat);
        }

    }

}
