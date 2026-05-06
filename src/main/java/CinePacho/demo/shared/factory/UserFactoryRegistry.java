package CinePacho.demo.shared.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.registerData.RegisterData;
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

    public void createSpecificEntity(UserType type, UserEntity user, RegisterData registrationData) {
        factories.get(type).createSpecificEntity(user, registrationData);
    }

    public UserFactory getFactory(UserType userType) {
        return factories.get(userType);
    }

}
