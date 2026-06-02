package CinePacho.demo.rooms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
 
import java.util.UUID;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {
 
    @NotNull(message = "El número de sala es obligatorio")
    private Integer numberRoom;
 
}
