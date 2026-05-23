package CinePacho.demo.seats.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import CinePacho.demo.seats.dto.request.SeatRequest;
import CinePacho.demo.seats.dto.response.SeatAvailabilitySummaryResponse;
import CinePacho.demo.seats.dto.response.SeatResponse;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.enumeration.SeatType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class SeatService {
 
    private final SeatRepository seatRepository;
    private final RoomManager roomManager;
    private final JwtService jwtUtil;
    private final Map<UUID, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private TaskScheduler taskScheduler;


    // GET ALL by room
    public List<SeatResponse> getAllByRoom(UUID roomId) {
        List<SeatEntity> allSeats = seatRepository.findByRoomId(roomId);
 
        return allSeats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // CAMBIAR ESTADO DE LA SILLA
    public SeatResponse toggleSeat(UUID seatId, String token) {

        //se extrae el id del usuario del token
        UUID userId = jwtUtil.extractUserId(token);

        //se busca el asiento por id
        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CinePachoException("Silla no encontrada "+ HttpStatus.NOT_FOUND.name()));

        //Se evalúa cada estado de la silla
        switch (seat.getStatus()) {

            case AVAILABLE -> {
                // bloquear
                seat.setStatus(SeatStatus.BLOCKED);
                seat.setBlockedByUserId(userId);
                seat.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
                seatRepository.save(seat);
                scheduleUnblock(seatId);   // timer de 10 min
            }

            case BLOCKED -> {
                // solo el mismo usuario puede desbloquear
                if (!seat.getBlockedByUserId().equals(userId)) {
                    throw new CinePachoException(
                            "Esta silla está reservada por otro usuario "+
                                    HttpStatus.CONFLICT.name()
                    );
                }
                // desbloquear
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBlockedByUserId(null);
                seat.setBlockedUntil(null);
                seatRepository.save(seat);
                cancelUnblock(seatId);    // cancela el timer
            }

            case SOLD -> throw new CinePachoException(
                    "Esta silla ya fue vendida "+
                            HttpStatus.CONFLICT.name()
            );
        }

        return toResponse(seat);
    }


    // Contador de 10 minutos antes de ejecutar forceUnblock
    private void scheduleUnblock(UUID seatId) {
        ScheduledFuture<?> timer = taskScheduler.schedule(
                () -> forceUnblock(seatId),
                Instant.now().plusSeconds(600)   // 10 minutos
        );
        timers.put(seatId, timer);
    }

    // Cancela el timer de desbloqueo
    private void cancelUnblock(UUID seatId) {
        ScheduledFuture<?> timer = timers.remove(seatId);
        if (timer != null) timer.cancel(false);
    }

    // desbloquea inmediatamente
    private void forceUnblock(UUID seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            if (seat.getStatus() == SeatStatus.BLOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBlockedByUserId(null);
                seat.setBlockedUntil(null);
                seatRepository.save(seat);
                timers.remove(seatId);
            }
        });
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
                .status(seat.getStatus())
                .build();
    }

    
}
 
