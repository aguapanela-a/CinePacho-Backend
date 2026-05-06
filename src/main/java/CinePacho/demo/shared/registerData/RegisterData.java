package CinePacho.demo.shared.registerData;

import CinePacho.demo.shared.enumeration.UserType;

public interface RegisterData {
    String name();
    String email();
    String password();
    UserType userType();
}
