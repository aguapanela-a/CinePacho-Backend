package CinePacho.demo.multiplex.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class MultiplexRequest {

    @NotBlank(message = "El nombre del multiplex es obligatorio")
    @Size(max = 100, message = "El nombre del multiplex no puede superar los 100 caracteres")
    private String nameMultiplex;
    
    @NotBlank(message = "La dirección del multiplex es obligatoria")
    @Size(max = 200, message = "La dirección del multiplex no puede superar los 200 caracteres")
    private String addressMultiplex;

    @NotBlank(message = "La ciudad del multiplex es obligatoria")
    @Size(max = 100, message = "La ciudad del multiplex no puede superar los 100 caracteres")
    private String cityMultiplex;

    //@Positive
    @Range(min = 5, max = 15, message = "La cantidad de salas debe ser entre 5 y 15 por multiplex")
    private Integer numberOfRooms;
}
