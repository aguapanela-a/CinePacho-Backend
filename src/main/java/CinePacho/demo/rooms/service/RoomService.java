package CinePacho.demo.rooms.service;


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
    private final SeatRepository seatRepository;   // ← inyectado ahora que existe
 
    // ── GET ALL ─────────────────────────────────────────────────────────────────
    public List<RoomResponse> obtenerTodas() {
        List<RoomResponse> rooms = roomRepository.findAll()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
 
        return rooms;
    }
 
    // ── GET BY ID ────────────────────────────────────────────────────────────────
    public RoomDetailResponse obtenerPorId(UUID id) {
        RoomEntity room = findOrThrow(id);
        return toDetail(room);
    }
 
    // ── CREATE ───────────────────────────────────────────────────────────────────
    public RoomDetailResponse crear(RoomRequest request) {
        if (roomRepository.existsByMultiplexIdAndNumberRoom(request.getMultiplexId(), request.getNumberRoom())) {
            throw new IllegalArgumentException(
                    "Ya existe una sala con el número " + request.getNumberRoom() + " en ese multiplex");
        }
 
        RoomEntity room = RoomEntity.builder()
                .multiplexId(request.getMultiplexId())
                .numberRoom(request.getNumberRoom())
                .generalCapacity(request.getGeneralCapacity())
                .preferentialCapacity(request.getPreferentialCapacity())
                .build();
 
        return toDetail(roomRepository.save(room));
    }
 
    // ── UPDATE ───────────────────────────────────────────────────────────────────
    public RoomDetailResponse actualizar(UUID id, RoomRequest request) {
        RoomEntity room = findOrThrow(id);
 
        boolean cambioNumero = !room.getNumberRoom().equals(request.getNumberRoom())
                || !room.getMultiplexId().equals(request.getMultiplexId());
 
        if (cambioNumero && roomRepository.existsByMultiplexIdAndNumberRoom(
                request.getMultiplexId(), request.getNumberRoom())) {
            throw new IllegalArgumentException(
                    "Ya existe una sala con el número " + request.getNumberRoom() + " en ese multiplex");
        }
 
        room.setMultiplexId(request.getMultiplexId());
        room.setNumberRoom(request.getNumberRoom());
        room.setGeneralCapacity(request.getGeneralCapacity());
        room.setPreferentialCapacity(request.getPreferentialCapacity());
 
        return toDetail(roomRepository.save(room));
    }
 
    // ── DELETE (lógico) ──────────────────────────────────────────────────────────
    public void eliminar(UUID id) {
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
                .idSala(room.getId().toString())
                .generalCapacity(room.getGeneralCapacity())
                .preferentialCapacity(room.getPreferentialCapacity())
                .isSalaActive(room.getActive())
                .build();
    }
 
    private RoomDetailResponse toDetail(RoomEntity room) {
        List<SeatAvailabilitySummaryResponse> seats = List.of(
                SeatAvailabilitySummaryResponse.builder()
                        .roomId(room.getId().toString())
                        .availableGeneral(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.GENERAL))
                        .availablePreferential(seatRepository.countByRoomIdAndType(room.getId(), CinePacho.demo.shared.enumeration.SeatType.PREFERENTIAL))
                        .totalAvailable(seatRepository.countByRoomIdAndType(room.getId(), null)) // Total sin filtrar por tipo
                        .build()
        );
 
        return RoomDetailResponse.builder()
                .idSala(room.getId().toString())
                .multiplexId(room.getMultiplexId().toString())
                .numberRoom(room.getNumberRoom())
                .generalCapacity(room.getGeneralCapacity())
                .preferentialCapacity(room.getPreferentialCapacity())
                .isSalaActive(room.getActive())
                .seats(seats)
                .build();
    }
}
 