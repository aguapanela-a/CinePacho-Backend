package CinePacho.demo.rooms.service;


import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.dto.response.RoomResponse;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.rooms.repository.RoomRepository;
import CinePacho.demo.seats.dto.response.SeatAvailabilitySummaryResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class RoomService {
 
    private final RoomRepository roomRepository;
    private final RoomManager roomManager;
    private final MultiplexProvider multiplexProvider;
    private final SeatManager seatManager;
    private final AccessValidator accessValidator;

    //Obtener todas las salas de un multiplex
    // ── GET ALL ─────────────────────────────────────────────────────────────────
    public List<RoomResponse> getAllByMultiplexId(UUID multiplexId) {
        return roomRepository.findByMultiplexId(multiplexId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }


    public RoomDetailResponse create(UUID multiplexId) {

        // Valida que el gerente sólo cree salas en su multiplex
        accessValidator.validateMultiplexAccess(multiplexId);

        //Crea y guarda una sala con todas sus sillas
        roomManager.createRoom(multiplexProvider.getMultiplexById(multiplexId));

        //Trae la ultima sala creada de este multiplex
        RoomEntity roomSaved = roomRepository.findTopByMultiplexIdOrderByCreatedAtDesc(multiplexId);

        return toDetail(roomSaved);
    }


    // ── DELETE (lógico) ──────────────────────────────────────────────────────────
    public void delete(UUID id) {
        RoomEntity room = findOrThrow(id);
        // Valida que el gerente sólo elimine salas de su multiplex
        accessValidator.validateMultiplexAccess(room.getMultiplex().getId());
        room.setActive(false);
        roomRepository.save(room);
    }
 
    // ── HELPERS ──────────────────────────────────────────────────────────────────
    private RoomEntity findOrThrow(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sala no encontrada con id: " + id));
    }
 
    private RoomResponse toSummary(RoomEntity room) {
        return RoomResponse.builder()
                .idRoom(room.getId())
                .isRoomActive(room.getActive())
                .roomNumber(room.getRoomNumber())
                .build();
    }



    private RoomDetailResponse toDetail(RoomEntity room) {
        List<SeatAvailabilitySummaryResponse> seats = List.of(
                SeatAvailabilitySummaryResponse.builder()
                        // Conteo de sillas delegando el acceso al módulo de sillas
                        .availableGeneral(seatManager.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.GENERAL))
                        .availablePreferential(seatManager.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.PREFERENTIAL))
                        .totalAvailable(seatManager.countByRoomId(room.getId())) // Total sin filtrar por tipo
                        .build()
        );
 
        return RoomDetailResponse.builder()
                .idRoom(room.getId())
                .isRoomActive(room.getActive())
                .seats(seats)
                .build();
    }
}
 
