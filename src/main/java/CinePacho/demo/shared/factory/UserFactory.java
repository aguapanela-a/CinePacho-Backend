package CinePacho.demo.shared.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.registerData.RegisterData;

//Interfaz genérica para crear todos los tipos de entidades
public interface UserFactory<T> {
    UserType getSupportedType();
    void createSpecificEntity(UserEntity entity, T registrationData);
}
