package CinePacho.demo.seats.service;

import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.seats.repository.SeatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Component
public class SeatUnblockScheduler {

    private final SeatRepository seatRepository;

    @Transactional
    public void forceUnblock(UUID seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {

            if (seat.getStatus() == SeatStatus.BLOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBlockedByUserEmail(null);
                seat.setBlockedUntil(null);
                seatRepository.save(seat);
            }
        });
    }
}
