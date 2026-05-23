package CinePacho.demo.rooms.service;


import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.dto.response.RoomResponse;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.rooms.repository.RoomRepository;
import CinePacho.demo.seats.dto.response.SeatAvailabilitySummaryResponse;
import CinePacho.demo.seats.repository.SeatRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class RoomService {
 
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final RoomManager roomManager;
    private final MultiplexProvider multiplexProvider;

    // ── GET ALL ─────────────────────────────────────────────────────────────────
    public List<RoomResponse> getAll() {
        return roomRepository.findAll()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
 
    // ── GET BY ID ────────────────────────────────────────────────────────────────
    public RoomDetailResponse getById(UUID id) {
        RoomEntity room = findOrThrow(id);
        return toDetail(room);
    }

    //TODO: consumir RoomManager.createRoom(UUID multiplexId) para crear una unica sala
    // ── CREATE ───────────────────────────────────────────────────────────────────
    public RoomDetailResponse create(UUID multiplexId) {

        //Crea y guarda una sala con todas sus sillas
        roomManager.createRoom(multiplexProvider.getMultiplexById(multiplexId));

        //Trae la ultima sala creada de este multiplex
        RoomEntity roomSaved = roomRepository.findTopByMultiplexIdOrderByCreatedAtDesc(multiplexId);

        return toDetail(roomSaved);
    }


    // ── DELETE (lógico) ──────────────────────────────────────────────────────────
    public void delete(UUID id) {
        RoomEntity room = findOrThrow(id);
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
                .build();
    }



    private RoomDetailResponse toDetail(RoomEntity room) {
        List<SeatAvailabilitySummaryResponse> seats = List.of(
                SeatAvailabilitySummaryResponse.builder()
                        .availableGeneral(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.GENERAL))
                        .availablePreferential(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.PREFERENTIAL))
                        .totalAvailable(seatRepository.countByRoomId(room.getId())) // Total sin filtrar por tipo
                        .build()
        );
 
        return RoomDetailResponse.builder()
                .idRoom(room.getId())
                .isRoomActive(room.getActive())
                .seats(seats)
                .build();
    }
}
 