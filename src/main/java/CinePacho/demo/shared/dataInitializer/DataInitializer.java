package CinePacho.demo.shared.dataInitializer;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// CommandLineRunner es una clase que ejecuta run() una unica vez después de que
// el contexto de la app termina de inicializarse, es decir, luego de todos los beans
// y la BD

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final SeatManager seatManager;

    @Autowired
    public DataInitializer(UserCreationService userCreationService, UserRepository userRepository, SeatManager seatManager) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
        this.seatManager = seatManager;
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

        // limpiar sillas bloqueadas huérfanas al arrancar
        seatManager.findByStatus(SeatStatus.BLOCKED).forEach(seat -> {
            if (seat.getBlockedUntil().isBefore(LocalDateTime.now())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBlockedByUserEmail(null);
                seat.setBlockedUntil(null);
                seatManager.save(seat);
            }
        });

    }


}
