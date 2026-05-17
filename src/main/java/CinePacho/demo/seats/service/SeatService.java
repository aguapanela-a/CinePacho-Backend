package CinePacho.demo.seats.service;

import CinePacho.demo.shared.auxiliaryClass.RoomProvider;
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
    private final RoomProvider roomProvider;
 
    // GET ALL by room
    public List<SeatResponse> obtenerTodosPorSala(UUID roomId) {
        List<SeatEntity> allSeats = seatRepository.findByRoomId(roomId);
 
        return allSeats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET availability totals by room and seat type
    public SeatAvailabilitySummaryResponse obtenerResumenDisponibilidadPorSala(UUID roomId) {
        long availableGeneral = seatRepository.countByRoomIdAndType(roomId, SeatType.GENERAL);
        long availablePreferential = seatRepository.countByRoomIdAndType(roomId, SeatType.PREFERENTIAL);

        return SeatAvailabilitySummaryResponse.builder()
                .roomId(roomId.toString())
                .availableGeneral(availableGeneral)
                .availablePreferential(availablePreferential)
                .totalAvailable(availableGeneral + availablePreferential)
                .build();
    }
 
    // GET BY ID
    public SeatResponse obtenerPorId(UUID id) {
        return toResponse(findOrThrow(id));
    }
 
    // CREATE
    public SeatResponse crear(SeatRequest request) {
        if (seatRepository.existsByRoomIdAndSeatNumber(request.getRoomId(), request.getSeatNumber())) {
            throw new IllegalArgumentException(
                    "Ya existe el asiento número " + request.getSeatNumber() + " en esa sala");
        }
 
        SeatEntity seat = SeatEntity.builder()
                .room(roomProvider.getRoom(request.getRoomId()))
                .seatNumber(request.getSeatNumber())
                .type(request.getType())
                .build();
 
        return toResponse(seatRepository.save(seat));
    }
 
    // UPDATE
    public SeatResponse actualizar(UUID id, SeatRequest request) {
        SeatEntity seat = findOrThrow(id);
 
        boolean cambioNumero = !seat.getSeatNumber().equals(request.getSeatNumber())
                || !seat.getRoom().getId().equals(request.getRoomId());
 
        if (cambioNumero && seatRepository.existsByRoomIdAndSeatNumber(
                request.getRoomId(), request.getSeatNumber())) {
            throw new IllegalArgumentException(
                    "Ya existe el asiento número " + request.getSeatNumber() + " en esa sala");
        }

        seat.setRoom(roomProvider.getRoom(request.getRoomId()));
        seat.setSeatNumber(request.getSeatNumber());
        seat.setType(request.getType());
 
        return toResponse(seatRepository.save(seat));
    }
 
    // DELETE (physical)
    public void eliminar(UUID id) {
        if (!seatRepository.existsById(id)) {
            throw new EntityNotFoundException("Asiento no encontrado con id: " + id);
        }
        seatRepository.deleteById(id);
    }
 
    // HELPERS
    private SeatEntity findOrThrow(UUID id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado con id: " + id));
    }
 
    private SeatResponse toResponse(SeatEntity seat) {
        return SeatResponse.builder()
                .idSeat(seat.getId().toString())
                .roomId(seat.getRoom().getId().toString())
                .seatNumber(seat.getSeatNumber())
                .type(seat.getType().name())
                .build();
    }
}
 