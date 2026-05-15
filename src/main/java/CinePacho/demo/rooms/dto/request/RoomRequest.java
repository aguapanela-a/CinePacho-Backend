package CinePacho.demo.rooms.dto.request;

import jakarta.annotation.PostConstruct;
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
 
    //Automatico ?
    @NotNull(message = "El número de sala es obligatorio")
    @Positive(message = "El número de sala debe ser mayor a 0")
    private Integer numberRoom;
 
    @NotNull(message = "La capacidad general es obligatoria")
    @Positive(message = "La capacidad general debe ser mayor a 0")
    private Integer generalCapacity;
 
    @NotNull(message = "La capacidad preferencial es obligatoria")
    @Positive(message = "La capacidad preferencial debe ser mayor a 0")
    private Integer preferentialCapacity;

}
