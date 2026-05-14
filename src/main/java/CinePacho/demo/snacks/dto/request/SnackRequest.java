package CinePacho.demo.snacks.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackRequest {
 
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;
 
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;
 
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal price;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private int quantity;
}
