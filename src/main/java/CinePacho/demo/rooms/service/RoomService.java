package CinePacho.demo.rooms.service;


import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.SeatGenerator;
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
    private final SeatGenerator seatGenerator;

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

        MultiplexEntity multiplex = multiplexProvider.obtenerMultiplexPorId(request.getMultiplexId());

        RoomEntity room = RoomEntity.builder()
                .multiplex(multiplex)
                .numberRoom(request.getNumberRoom())
                .generalCapacity(request.getGeneralCapacity())
                .preferentialCapacity(request.getPreferentialCapacity())
                .build();

        //Guardar entidad sala en BD
        RoomEntity roomSaved = roomRepository.save(room);

        //Crear e insertar sillas físicas asociadas a esa sala
        seatGenerator.createSeat(request.getGeneralCapacity(), request.getPreferentialCapacity(),roomSaved.getId());

        return toDetail(roomSaved);
    }
 
    // ── UPDATE ───────────────────────────────────────────────────────────────────
    public RoomDetailResponse update(UUID id, RoomRequest request) {
        RoomEntity room = findOrThrow(id);
 
        boolean cambioNumero = !room.getNumberRoom().equals(request.getNumberRoom())
                || !room.getMultiplex().getId().equals(request.getMultiplexId());
 
        if (cambioNumero && roomRepository.existsByMultiplexIdAndNumberRoom(
                request.getMultiplexId(), request.getNumberRoom())) {
            throw new IllegalArgumentException(
                    "Ya existe una sala con el número " + request.getNumberRoom() + " en ese multiplex");
        }

        room.setMultiplex(multiplexProvider.obtenerMultiplexPorId(request.getMultiplexId()));
        room.setNumberRoom(request.getNumberRoom());
        room.setGeneralCapacity(request.getGeneralCapacity());
        room.setPreferentialCapacity(request.getPreferentialCapacity());
 
        return toDetail(roomRepository.save(room));
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
                        .roomId(room.getId().toString())
                        .availableGeneral(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.GENERAL))
                        .availablePreferential(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.PREFERENTIAL))
                        .totalAvailable(seatRepository.countByRoomId(room.getId())) // Total sin filtrar por tipo
                        .build()
        );
 
        return RoomDetailResponse.builder()
                .idRoom(room.getId().toString())
                .numberRoom(room.getNumberRoom())
                .generalCapacity(room.getGeneralCapacity())
                .preferentialCapacity(room.getPreferentialCapacity())
                .isRoomActive(room.getActive())
                .seats(seats)
                .build();
    }
}
 