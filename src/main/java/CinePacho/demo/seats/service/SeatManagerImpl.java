package CinePacho.demo.seats.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.seats.repository.SeatRepository;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.SeatType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class SeatManagerImpl implements SeatManager {

    private final SeatRepository seatRepository;
    private final Map<UUID, ScheduledFuture<?>> scheduledReleases = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager;

    public SeatManagerImpl(SeatRepository seatRepository, TaskScheduler taskScheduler, CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager) {
        this.seatRepository = seatRepository;
        this.taskScheduler = taskScheduler;
        this.seatScreeningManager = seatScreeningManager;
    }


    @Override
    public void createSeat(int general, int preferential, RoomEntity room) {

        // Crear sillas generales
        for (int i = 0; i < general; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i);
            seat.setType(SeatType.GENERAL);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setRoom(room);

            seatRepository.save(seat);
        }

        //Crear sillas preferenciales
        for (int i = 0; i < preferential; i++) {
            SeatEntity seat = new SeatEntity();

            seat.setSeatNumber(i + general);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setType(SeatType.PREFERENTIAL);
            seat.setRoom(room);

            seatRepository.save(seat);
        }

    }

    @Override
    public List<SeatEntity> findByStatus(SeatStatus status) {
        return seatRepository.findByStatus(status);
    }

    @Override
    public SeatEntity save(SeatEntity seat) {
        return seatRepository.save(seat);
    }

    @Override
    public Integer countByRoomIdAndType(UUID roomId, SeatType type) {
        // Encapsula el acceso al repositorio de sillas para otros módulos
        return seatRepository.countByRoomIdAndType(roomId, type);
    }

    @Override
    public Integer countByRoomId(UUID roomId) {
        // Encapsula el acceso al repositorio de sillas para otros módulos
        return seatRepository.countByRoomId(roomId);
    }

    @Override
    public List<SeatEntity> findAllByIdWithRoomAndMultiplex(List<UUID> ids) {
        // Evita el LazyLoading en validaciones de compra
        return seatRepository.findAllByIdWithRoomAndMultiplex(ids);
    }

    @Override
    public void updateSeatStatus(UUID seatId, SeatStatus status) {
        SeatEntity seat = seatRepository.getReferenceById(seatId);
        seatRepository.save(seat);
    }

    //programar la liberación de las sillas 3 horas después de iniciar la función
    @Override
    public void scheduleRelease(UUID screeningId, UUID roomId, LocalDateTime screeningStartTime) {

        // Evita programar la misma función dos veces
        if (scheduledReleases.containsKey(screeningId)) {
            System.out.printf("Esta funcion " + screeningId + " ya fue programada para liberar sillas a las " + screeningStartTime.plusHours(3) + "");
            return;
        }

        // Calcula cuándo deben liberarse las sillas (3 horas después del inicio)
        Instant releaseTime = screeningStartTime
                .plusHours(3)
                .atZone(ZoneId.of("America/Bogota"))
                .toInstant();

        System.out.printf("funcion prgoramada para liberar sillas en %s", releaseTime);

        // Si ese momento ya pasó, libera inmediatamente sin programar
        if (releaseTime.isBefore(Instant.now(Clock.system(ZoneId.of("America/Bogota"))))) {
            // liberar las reservas específicas de la función
            this.seatScreeningManager.releaseAllSeatsForScreening(screeningId);
            System.out.printf("-------- Liberando sillas inmediatamente: " + Instant.now());
            return;
        }

        // Programa la tarea para ejecutarse exactamente en releaseTime (ScheduledFuture es un timer activo)
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    // libera las reservas de la función
                    this.seatScreeningManager.releaseAllSeatsForScreening(screeningId);
                    scheduledReleases.remove(screeningId);           // limpia el timer del mapa
                },
                releaseTime // en este instante
        );

        // Guarda el timer asociado a esa función
        scheduledReleases.put(screeningId, future);
    }

    @Override
    //Liberar todas las sillas de una sala
    public void releaseAllSeatsInRoom(UUID roomId) {
        List<SeatEntity> seats = seatRepository.findByRoomId(roomId);
        seats.forEach(
                seat -> {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setBlockedByUserEmail(null);
                    seat.setBlockedUntil(null);
                }
        );
        seatRepository.saveAll(seats);
    }

    @Override
    public SeatEntity getSeatById(UUID seatId) {
        return seatRepository.findById(seatId).orElseThrow(()-> new CinePachoException("Silla no encontrada"));
    }

    @Override
    public String getSeatNumber(UUID seatId) {
        return seatRepository.findById(seatId).orElseThrow(()-> new CinePachoException("Silla no encontrada")).getSeatNumber().toString();
    }


}
