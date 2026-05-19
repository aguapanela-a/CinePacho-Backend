package CinePacho.demo.multiplex.service;

import CinePacho.demo.exception.CinePachoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import CinePacho.demo.multiplex.dto.request.MultiplexRequest;
import CinePacho.demo.multiplex.dto.response.MultiplexDetailResponse;
import CinePacho.demo.multiplex.dto.response.MultiplexSummaryResponse;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.dto.response.RoomResponse;
import CinePacho.demo.rooms.repository.RoomRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class MultiplexService {
 
    private final MultiplexRepository multiplexRepository;
    private final RoomRepository roomRepository;   
 
    // ── GET ALL ──────────────────────────────────────────────────────────────────
    public List<MultiplexSummaryResponse> getAll() {
        List<MultiplexSummaryResponse> list = multiplexRepository.findAll()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
 
        return list;
    }
 
    // ── GET BY ID ────────────────────────────────────────────────────────────────
    public MultiplexDetailResponse getById(UUID id) {
        MultiplexEntity multiplex = findOrThrow(id);
        return toDetail(multiplex);
    }
 
    // ── CREATE ───────────────────────────────────────────────────────────────────
    public MultiplexDetailResponse create(MultiplexRequest request) {
        if (multiplexRepository.existsByNameAndCity(request.getNameMultiplex(), request.getCityMultiplex())) {
            throw new CinePachoException(
                    "Ya existe un multiplex con el nombre '" + request.getNameMultiplex()
                    + "' en la ciudad " + request.getCityMultiplex());
        }
 
        MultiplexEntity multiplex = MultiplexEntity.builder()
                .name(request.getNameMultiplex())
                .address(request.getAddressMultiplex())
                .city(request.getCityMultiplex())
                .build();
 
        return toDetail(multiplexRepository.save(multiplex));
    }
 
    // ── UPDATE ───────────────────────────────────────────────────────────────────
    public MultiplexDetailResponse update(UUID id, MultiplexRequest request) {
        MultiplexEntity multiplex = findOrThrow(id);
 
        boolean cambioDatos = !multiplex.getName().equals(request.getNameMultiplex())
                || !multiplex.getCity().equals(request.getCityMultiplex());
 
        if (cambioDatos && multiplexRepository.existsByNameAndCity(
                request.getNameMultiplex(), request.getCityMultiplex())) {
            throw new IllegalArgumentException(
                    "Ya existe un multiplex con el nombre '" + request.getNameMultiplex()
                    + "' en la ciudad " + request.getCityMultiplex());
        }
 
        multiplex.setName(request.getNameMultiplex());
        multiplex.setAddress(request.getAddressMultiplex());
        multiplex.setCity(request.getCityMultiplex());
 
        return toDetail(multiplexRepository.save(multiplex));
    }
 
    // ── DELETE (físico) ──────────────────────────────────────────────────────────
    public void delete(UUID id) {
        if (!multiplexRepository.existsById(id)) {
            throw new EntityNotFoundException("Multiplex no encontrado con id: " + id);
        }
        multiplexRepository.deleteById(id);
    }
 








    // ── HELPERS ──────────────────────────────────────────────────────────────────
    private MultiplexEntity findOrThrow(UUID id) {
        return multiplexRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Multiplex no encontrado con id: " + id));
    }
 
    private MultiplexSummaryResponse toSummary(MultiplexEntity m) {
        return MultiplexSummaryResponse.builder()
                .idMultiplex(m.getId().toString())
                .nameMultiplex(m.getName())
                .cityMultiplex(m.getCity())
                .build();
    }
 
    private MultiplexDetailResponse toDetail(MultiplexEntity m) {
        List<RoomResponse> rooms = roomRepository.findByMultiplexId(m.getId())
                .stream()
                .map(r -> RoomResponse.builder()
                        .idRoom(r.getId())
                        .isRoomActive(r.getActive())
                        .build())
                .collect(Collectors.toList());
 
        return MultiplexDetailResponse.builder()
                .idMultiplex(m.getId().toString())
                .nameMultiplex(m.getName())
                .addressMultiplex(m.getAddress())
                .cityMultiplex(m.getCity())
                .rooms(rooms)
                .build();
    }
}