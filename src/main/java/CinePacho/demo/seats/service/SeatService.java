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
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager;
    private final Map<UUID, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final SeatUnblockScheduler seatUnblockScheduler;


    //TODO: ahcer que esto ahora genere la respuesta según la srcreening y el room
    // GET ALL by room
    public List<SeatResponse> getAllByRoom(UUID roomId) {
        List<SeatEntity> allSeats = seatRepository.findByRoomId(roomId);
 
        return allSeats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    // CAMBIAR ESTADO DE LA SILLA (ahora por función)
    public SeatResponse toggleSeat(UUID seatId, String token, java.util.UUID screeningId) {

        //se extrae el email del usuario del token
        String userEmail = jwtUtil.extractEmail(token);

        // delegar la lógica a SeatScreeningManager que maneja bloqueo por función
        var ss = seatScreeningManager.toggleSeatForScreening(seatId, screeningId, userEmail);

        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CinePachoException("Silla no encontrada " + HttpStatus.NOT_FOUND.name()));

        SeatResponse response = toResponse(seat);
        // sobreescribir el status con el estado específico para la función
        response.setStatus(ss.getStatus());
        return response;
    }


    // Contador de 10 minutos antes de ejecutar forceUnblock
    private void scheduleUnblock(UUID seatId) {
        ScheduledFuture<?> timer = taskScheduler.schedule(
                () -> seatUnblockScheduler.forceUnblock(seatId),
                Instant.now(Clock.system(ZoneId.of("America/Bogota"))).plusSeconds(600)   // 10 minutos
        );
        timers.put(seatId, timer);
    }

    // Cancela el timer de desbloqueo
    private void cancelUnblock(UUID seatId) {
        ScheduledFuture<?> timer = timers.remove(seatId);
        if (timer != null) timer.cancel(false);
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
 

    // Disponibilidad de un asiento específico
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
 
