package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.auth.entities.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public interface UserManager {
    UserEntity getUserByEmail(String email);
}
