package CinePacho.demo.auth.service;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.shared.user.UserRepository;

import org.springframework.stereotype.Service;


@Service
public class UserManagerImpl implements UserManager {

    private final UserRepository userRepository;

    public UserManagerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CinePachoException("Usuario no encontrado con email: " + email));
        
    }
}

