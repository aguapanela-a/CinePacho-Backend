package CinePacho.demo.rooms.service;


import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import CinePacho.demo.rooms.dto.request.RoomRequest;
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
    private final MultiplexProvider  multiplexProvider;
    private final SeatManager seatManager;

    private final int generalCapacity = 40;
    private final int preferencialCapacity = 20;

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
 
    // ── CREATE ───────────────────────────────────────────────────────────────────
    public RoomDetailResponse create(RoomRequest request) {
        if (roomRepository.existsByMultiplexIdAndNumberRoom(request.getMultiplexId(), request.getNumberRoom())) {
            throw new IllegalArgumentException(
                    "Ya existe una sala con el número " + request.getNumberRoom() + " en ese multiplex");
        }

        MultiplexEntity multiplex = multiplexProvider.getMultiplexById(request.getMultiplexId());

        RoomEntity room = RoomEntity.builder()
                .multiplex(multiplex)
                .numberRoom(request.getNumberRoom())
                .generalCapacity(generalCapacity)
                .preferentialCapacity(preferencialCapacity)
                .build();

        //Guardar entidad sala en BD
        RoomEntity roomSaved = roomRepository.save(room);

        //Crear e insertar sillas físicas asociadas a esa sala
        seatManager.createSeat(generalCapacity, preferencialCapacity,roomSaved.getId());

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
                .idRoom(room.getId().toString())
                .generalCapacity(room.getGeneralCapacity())
                .preferentialCapacity(room.getPreferentialCapacity())
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
                .idRoom(room.getId().toString())
                .numberRoom(room.getNumberRoom())
                .isRoomActive(room.getActive())
                .seats(seats)
                .build();
    }
}
 