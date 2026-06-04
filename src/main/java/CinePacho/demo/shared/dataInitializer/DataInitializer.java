package CinePacho.demo.shared.dataInitializer;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.points.entities.PointsConfigEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import CinePacho.demo.shared.auxiliaryClass.PointsManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

// CommandLineRunner es una clase que ejecuta run() una unica vez después de que
// el contexto de la app termina de inicializarse, es decir, luego de todos los beans
// y la BD

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final SeatManager seatManager;
    private final MovieManager movieManager;
    private final CinePacho.demo.seats.repository.SeatScreeningRepository seatScreeningRepository;
    private final CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager;
    private final PointsManager pointsManager;

    @Autowired
    public DataInitializer(UserCreationService userCreationService, UserRepository userRepository, SeatManager seatManager, MovieManager movieManager, CinePacho.demo.seats.repository.SeatScreeningRepository seatScreeningRepository, CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager, PointsManager pointsManager) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
        this.seatManager = seatManager;
        this.movieManager = movieManager;
        this.seatScreeningRepository = seatScreeningRepository;
        this.seatScreeningManager = seatScreeningManager;
        this.pointsManager = pointsManager;
    }

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @Override
    public void run(String... args) throws Exception {

        if(!userRepository.existsByEmail("admin@cinepacho.com")){
            UserEntity admin = userCreationService.createUser(
                    adminName,
                    adminPassword,
                    UserType.ADMIN,
                    adminEmail,
                    null);

            admin.setEnabled(true);

            userRepository.save(admin);
        }


        if ( pointsManager.getID() == null) {
            PointsConfigEntity pointsConfig = PointsConfigEntity.builder().byUnit(true).build();
            pointsManager.save(pointsConfig);
        }



        // limpiar sillas bloqueadas huérfanas al arrancar (globales)
        seatManager.findByStatus(SeatStatus.BLOCKED).forEach(seat -> {
            System.out.printf("--------------Liberando silla %s", seat.getId());
            if (seat.getBlockedUntil().isBefore(LocalDateTime.now(ZoneId.of("America/Bogota")))) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBlockedByUserEmail(null);
                seat.setBlockedUntil(null);
                seatManager.save(seat);
            }
        });

        // limpiar reservas por función que hayan expirado
        seatScreeningRepository.findByStatus(SeatStatus.BLOCKED).forEach(ss -> {
            System.out.printf("--------------Liberando reserva por función %s", ss.getId());
            if (ss.getBlockedUntil() != null && ss.getBlockedUntil().isBefore(LocalDateTime.now(ZoneId.of("America/Bogota")))){
                ss.setStatus(SeatStatus.AVAILABLE);
                ss.setBlockedByUserEmail(null);
                ss.setBlockedUntil(null);
                seatScreeningRepository.save(ss);
            }
        });

        //  reprogramar funciones pendientes para liberar sillas ----
        // tomo la hora actual
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Bogota"));

        //Busco las funciones que aún no han liberado las sillas (funciones que empezaron hace menos de 3 horas O que aún no han empezado)
        List<MovieScreening> pendingScreenings = movieManager.findByDateTimeAfter(now.minusHours(3));

        //por cada función pendiente a liberar sillas -> reprogramar la tarea de liberar sillar 3 horas después del inicio de la función
        pendingScreenings.forEach(screening -> seatManager.scheduleRelease(
                screening.getId(),
                screening.getRoom().getId(),
                screening.getDateTime()
        ));

        // liberar las sillas que ya cumplen con la hora de liberación de sillas
        List<MovieScreening> completedScreenings = movieManager.findByDateBefore(now.minusHours(3));
        completedScreenings.forEach(movieScreening -> {
            seatManager.releaseAllSeatsInRoom(movieScreening.getRoom().getId());
            //marca la función como completada
            movieScreening.setStatus(ScreeningStatus.COMPLETED);
            //marca la sala como activa
            movieScreening.getRoom().setActive(true);

            movieManager.save(movieScreening);

        });
    }


}
