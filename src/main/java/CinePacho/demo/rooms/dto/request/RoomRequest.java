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
 
    @NotNull(message = "El id del multiplex es obligatorio")
    private UUID multiplexId;
 
}
