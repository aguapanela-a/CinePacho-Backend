package CinePacho.demo.shared.user;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactory;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import CinePacho.demo.shared.registerData.RegisterData;
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

        //Guardo UserEntity para generar el UUID
        UserEntity savedUser = userRepository.save(userEntity);


        //Si si existe la factory cree la entidad concreta
        if (userFactoryRegistry.getFactory(userEntity.getUserType()) != null) {
            userFactoryRegistry.createSpecificEntity(savedUser.getUserType(),savedUser, extraData);
        }

        return savedUser;
    }

//    public UserEntity findUserByUsername(String username) {
//        return userRepository.findUserEntityByUsername(username);
//    }
}
