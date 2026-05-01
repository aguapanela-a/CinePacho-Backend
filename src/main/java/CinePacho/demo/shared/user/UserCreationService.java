package CinePacho.demo.shared.user;

import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.auth.repository.UserRepository;
import CinePacho.demo.shared.enumeration.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserCreationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserCreationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity createUser(String name, String password, UserType userType) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(name);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setUserType(userType);

        return userRepository.save(userEntity);
    }

    public UserEntity findUserByUsername(String username) {
        return userRepository.findUserEntityByUsername(username);
    }
}
