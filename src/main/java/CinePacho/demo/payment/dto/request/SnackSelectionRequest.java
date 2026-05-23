package CinePacho.demo.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSelectionRequest {

    // ID del snack seleccionado
    @NotNull(message = "El id del snack es obligatorio")
    private UUID snackId;

    // Cantidad solicitada
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mínimo 1")
    private Integer quantity;
}
