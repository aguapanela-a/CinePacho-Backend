package CinePacho.demo.shared.user;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.dto.request.UpdateEmployeeRequestDTO;
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

        //Guardo UserEntity para generar el UUID
        UserEntity savedUser = userRepository.save(userEntity);


        //Si si existe la factory cree la entidad concreta
        if (userFactoryRegistry.getFactory(userEntity.getUserType()) != null) {
            userFactoryRegistry.createSpecificEntity(savedUser.getUserType(),savedUser, extraData);
        }

        return savedUser;
    }

    public void updateUser(UserEntity user, String name, UserType userType, String email, UpdateEmployeeRequestDTO dto) {
    user.setUsername(name);
    user.setUserType(userType);
    user.setEmail(email);
    
    // Validar aquí también si el DTO contiene una contraseña nueva
    if (dto.password() != null && !dto.password().trim().isEmpty()) {
        // Supongamos que usas BCrypt para la seguridad de Cine Pacho:
        String passwordEncriptada = passwordEncoder.encode(dto.password());
        user.setPassword(passwordEncriptada);
    }
    // Si la contraseña viene de frontend como "" (vacía), este bloque se salta
    // y la contraseña vieja del 'user' se queda intacta en la base de datos.
}

//    public UserEntity findUserByUsername(String username) {
//        return userRepository.findUserEntityByUsername(username);
//    }


}
