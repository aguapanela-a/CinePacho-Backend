package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.seats.entities.SeatScreeningEntity;

import java.util.UUID;

public interface SeatScreeningManager {
    SeatScreeningEntity toggleSeatForScreening(UUID seatId, UUID screeningId, String userEmail);
    SeatScreeningEntity getSeatScreening(UUID seatId, UUID screeningId);
    void markSold(UUID seatId, UUID screeningId);
    void releaseAllSeatsForScreening(UUID screeningId);
}
