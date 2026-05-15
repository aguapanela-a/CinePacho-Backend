package CinePacho.demo.shared.dataInitializer;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// CommandLineRunner es una clase que ejecuta run() una unica vez después de que
// el contexto de la app termina de inicializarse, es decir, luego de todos los beans
// y la BD
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserCreationService userCreationService;
    private final UserRepository userRepository;

    @Autowired
    public DataInitializer(UserCreationService userCreationService, UserRepository userRepository) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
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

    }
}
