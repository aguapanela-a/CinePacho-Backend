package CinePacho.demo.seats.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.entities.SeatScreeningEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.seats.repository.SeatScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class SeatScreeningManagerImpl implements SeatScreeningManager {

    private final SeatScreeningRepository seatScreeningRepository;
    private final SeatRepository seatRepository;
    private final MovieScreeningRepository movieScreeningRepository;
    private final TaskScheduler taskScheduler;

    // timers keyed by "seatId:screeningId"
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    private String key(UUID seatId, UUID screeningId){
        return seatId.toString() + ":" + screeningId.toString();
    }

    @Override
    @Transactional
    public SeatScreeningEntity toggleSeatForScreening(UUID seatId, UUID screeningId, String userEmail) {
        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CinePachoException("Silla no encontrada"));
        MovieScreening screening = movieScreeningRepository.findById(screeningId)
                .orElseThrow(() -> new CinePachoException("Función no encontrada"));

        Optional<SeatScreeningEntity> optional = seatScreeningRepository.findBySeat_IdAndScreening_Id(seatId, screeningId);
        SeatScreeningEntity entity;
        if (optional.isEmpty()) {
            // crear y bloquear
            entity = SeatScreeningEntity.builder()
                    .seat(seat)
                    .screening(screening)
                    .status(SeatStatus.BLOCKED)
                    .blockedByUserEmail(userEmail)
                    .blockedUntil(LocalDateTime.now().plusMinutes(10))
                    .build();
            seatScreeningRepository.save(entity);
            scheduleUnblock(key(seatId, screeningId), seatId, screeningId, 600);
            return entity;
        }

        entity = optional.get();
        switch (entity.getStatus()){
            case AVAILABLE:
                entity.setStatus(SeatStatus.BLOCKED);
                entity.setBlockedByUserEmail(userEmail);
                entity.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
                seatScreeningRepository.save(entity);
                scheduleUnblock(key(seatId, screeningId), seatId, screeningId, 600);
                break;
            case BLOCKED:
                if (entity.getBlockedByUserEmail() == null || !entity.getBlockedByUserEmail().equals(userEmail)){
                    throw new CinePachoException("Esta silla está reservada por otro usuario");
                }
                entity.setStatus(SeatStatus.AVAILABLE);
                entity.setBlockedByUserEmail(null);
                entity.setBlockedUntil(null);
                seatScreeningRepository.save(entity);
                cancelUnblock(key(seatId, screeningId));
                break;
            case SOLD:
                throw new CinePachoException("Esta silla ya fue vendida");
        }

        return entity;
    }

    @Override
    public SeatScreeningEntity getSeatScreening(UUID seatId, UUID screeningId) {
        return seatScreeningRepository.findBySeat_IdAndScreening_Id(seatId, screeningId).orElse(null);
    }

    @Override
    @Transactional
    public void markSold(UUID seatId, UUID screeningId) {
        SeatScreeningEntity entity = seatScreeningRepository.findBySeat_IdAndScreening_Id(seatId, screeningId)
                .orElseThrow(() -> new CinePachoException("La silla no ha sido seleccionada previamente para esta función"));
        if (entity.getStatus() != SeatStatus.BLOCKED){
            throw new CinePachoException("La silla no está bloqueada para esta función");
        }
        entity.setStatus(SeatStatus.SOLD);
        entity.setBlockedByUserEmail(null);
        entity.setBlockedUntil(null);
        seatScreeningRepository.save(entity);
        cancelUnblock(key(seatId, screeningId));
    }

    @Override
    @Transactional
    public void releaseAllSeatsForScreening(UUID screeningId) {
        List<SeatScreeningEntity> list = seatScreeningRepository.findByScreening_Id(screeningId);
        list.forEach(s -> {
            s.setStatus(SeatStatus.AVAILABLE);
            s.setBlockedByUserEmail(null);
            s.setBlockedUntil(null);
        });
        seatScreeningRepository.saveAll(list);

        // cancel timers
        // collect keys that end with ":screeningId"
        String suffix = ":" + screeningId.toString();
        List<String> keys = new ArrayList<>();
        for (String k : timers.keySet()) if (k.endsWith(suffix)) keys.add(k);
        keys.forEach(this::cancelUnblock);
    }

    private void scheduleUnblock(String key, UUID seatId, UUID screeningId, long seconds){
        ScheduledFuture<?> timer = taskScheduler.schedule(
                () -> forceUnblock(seatId, screeningId),
                Instant.now(Clock.system(ZoneId.of("America/Bogota"))).plusSeconds(seconds)
        );
        timers.put(key, timer);
    }

    private void cancelUnblock(String key){
        ScheduledFuture<?> timer = timers.remove(key);
        if (timer != null) timer.cancel(false);
    }

    @Transactional
    public void forceUnblock(UUID seatId, UUID screeningId){
        seatScreeningRepository.findBySeat_IdAndScreening_Id(seatId, screeningId).ifPresent(entity -> {
            if (entity.getStatus() == SeatStatus.BLOCKED) {
                entity.setStatus(SeatStatus.AVAILABLE);
                entity.setBlockedByUserEmail(null);
                entity.setBlockedUntil(null);
                seatScreeningRepository.save(entity);
            }
        });
        cancelUnblock(key(seatId, screeningId));
    }
}
