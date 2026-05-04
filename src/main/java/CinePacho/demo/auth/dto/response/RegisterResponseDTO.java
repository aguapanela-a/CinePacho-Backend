package CinePacho.demo.auth.dto.response;

import lombok.Setter;
import CinePacho.demo.shared.enumeration.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Setter
public class RegisterResponseDTO {

    private String token;
    private UserType userType;
    private String username;
    
    private String message;
}

