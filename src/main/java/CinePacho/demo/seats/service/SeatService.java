package CinePacho.demo.seats.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import CinePacho.demo.seats.dto.request.SeatRequest;
import CinePacho.demo.seats.dto.response.SeatAvailabilitySummaryResponse;
import CinePacho.demo.seats.dto.response.SeatResponse;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.enumeration.SeatType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class SeatService {
 
    private final SeatRepository seatRepository;
    private final RoomManager roomManager;
 
    // GET ALL by room
    public List<SeatResponse> getAllByRoom(UUID roomId) {
        List<SeatEntity> allSeats = seatRepository.findByRoomId(roomId);
 
        return allSeats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET availability totals by room and seat type
    public SeatAvailabilitySummaryResponse getSeatAvailabilitySummaryResponse(UUID roomId) {
        Integer availableGeneral = seatRepository.countByRoomIdAndType(roomId, SeatType.GENERAL);
        Integer availablePreferential = seatRepository.countByRoomIdAndType(roomId, SeatType.PREFERENTIAL);

        return SeatAvailabilitySummaryResponse.builder()
                .availableGeneral(availableGeneral)
                .availablePreferential(availablePreferential)
                .totalAvailable(availableGeneral + availablePreferential)
                .build();
    }


    public SeatResponse changeState(UUID seatId) {
        SeatEntity seat = seatRepository.findById(seatId).orElseThrow(()-> new CinePachoException("Asiento no encontrado"));
        seat.setAvailable(!seat.isAvailable());

        return toResponse(seatRepository.save(seat));
    }

 
    // GET BY ID
    public SeatResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }
 
    // CREATE
    public SeatResponse create(SeatRequest request) {
        if (seatRepository.existsByRoomIdAndSeatNumber(request.getRoomId(), request.getSeatNumber())) {
            throw new CinePachoException(
                    "Ya existe el asiento número " + request.getSeatNumber() + " en esa sala");
        }
 
        SeatEntity seat = SeatEntity.builder()
                .room(roomManager.getRoom(request.getRoomId()))
                .seatNumber(request.getSeatNumber())
                .type(request.getType())
                .build();
 
        return toResponse(seatRepository.save(seat));
    }
 
    // UPDATE
    public SeatResponse update(UUID id, SeatRequest request) {
        SeatEntity seat = findOrThrow(id);
 
        boolean cambioNumero = !seat.getSeatNumber().equals(request.getSeatNumber())
                || !seat.getRoom().getId().equals(request.getRoomId());
 
        if (cambioNumero && seatRepository.existsByRoomIdAndSeatNumber(
                request.getRoomId(), request.getSeatNumber())) {
            throw new IllegalArgumentException(
                    "Ya existe el asiento número " + request.getSeatNumber() + " en esa sala");
        }

        seat.setRoom(roomManager.getRoom(request.getRoomId()));
        seat.setSeatNumber(request.getSeatNumber());
        seat.setType(request.getType());
 
        return toResponse(seatRepository.save(seat));
    }
 
    // DELETE (physical)
    public void delete(UUID id) {
        if (!seatRepository.existsById(id)) {
            throw new EntityNotFoundException("Asiento no encontrado con id: " + id);
        }
        seatRepository.deleteById(id);
    }

    public void verifyAvailability(UUID seatId) {
        SeatEntity seat = findOrThrow(seatId);
        if (!seat.isAvailable()) {
            throw new CinePachoException("El asiento con id " + seatId + " no está disponible");
        }
        
        // Aquí podrías marcar el asiento como no disponible si quieres reservarlo
        seat.setAvailable(false);
        seatRepository.save(seat);

    }
 
    // HELPERS
    private SeatEntity findOrThrow(UUID id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new CinePachoException("Asiento no encontrado con id: " + id));
    }
 
    private SeatResponse toResponse(SeatEntity seat) {
        return SeatResponse.builder()
                .idSeat(seat.getId().toString())
                .roomId(seat.getRoom().getId().toString())
                .seatNumber(seat.getSeatNumber())
                .type(seat.getType().name())
                .isAvailable(seat.isAvailable())
                .build();
    }

    
}
 
