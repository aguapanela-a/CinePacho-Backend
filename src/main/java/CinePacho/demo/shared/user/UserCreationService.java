package CinePacho.demo.shared.user;

import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.auth.repository.UserRepository;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserCreationService {
    private final UserRepository userRepository;
    private final UserFactoryRegistry userFactoryRegistry;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserCreationService(UserRepository userRepository, UserFactoryRegistry userFactoryRegistry, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFactoryRegistry = userFactoryRegistry;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity createUser(String name, String password, UserType userType, String email, Object extraData ) {
        //Crear UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(name);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setUserType(userType);
        userEntity.setEmail(email);

        //crear entidad concreta
        userFactoryRegistry.createSpecificEntity(userEntity.getUserType(),userEntity, extraData);

        return userRepository.save(userEntity);
    }

    public UserEntity findUserByUsername(String username) {
        return userRepository.findUserEntityByUsername(username);
    }
}
