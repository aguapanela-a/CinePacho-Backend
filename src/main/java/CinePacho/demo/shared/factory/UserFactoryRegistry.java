package CinePacho.demo.shared.factory;

import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.shared.enumeration.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Orquesator de Factories según el tipo de usuario

@Component
public class UserFactoryRegistry {
    private final Map<UserType, UserFactory> factories;

    @Autowired
    public UserFactoryRegistry(List<UserFactory> factories) {
        this.factories = factories.stream()
                .collect(Collectors.toMap(
                        UserFactory::getSupportedType,
                        userFactory -> userFactory
                ));
    }

    public void createSpecificEntity(UserType type, UserEntity user, Object registrationData) {
        factories.get(type).createSpecificEntity(user, registrationData);
    }

}
