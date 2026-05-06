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

    public UserEntity createUser(String name, String password, UserType userType, String email, RegisterData extraData ) {
        //Crear UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(name);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setUserType(userType);
        userEntity.setEmail(email);

        UserFactory factory = userFactoryRegistry.getFactory(userEntity.getUserType());

        if (factory == null) {
            throw new CinePachoException(
                "No existe una factory para el tipo de usuario: " + userEntity.getUserType()
            );
        }

        //crear entidad concreta
        userFactoryRegistry.createSpecificEntity(userEntity.getUserType(),userEntity, extraData);

        return userRepository.save(userEntity);
    }

//    public UserEntity findUserByUsername(String username) {
//        return userRepository.findUserEntityByUsername(username);
//    }
}
