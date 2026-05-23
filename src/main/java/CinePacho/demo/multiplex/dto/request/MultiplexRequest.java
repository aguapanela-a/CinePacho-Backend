package CinePacho.demo.multiplex.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;
import java.math.BigDecimal;

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

    // Precios por tipo de silla (si no llegan, se usan valores por defecto)
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio general debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio general debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal generalSeatPrice;

    // Precio preferencial editable por admin/gerente
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio preferencial debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio preferencial debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal preferentialSeatPrice;
}
