package CinePacho.demo.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    // ID de la función (MovieScreening) que se está comprando
    @NotNull(message = "El id de la función es obligatorio")
    private UUID screeningId;

    // Lista de sillas seleccionadas (obligatoria)
    @NotEmpty(message = "Debe seleccionar al menos una silla")
    @Valid
    private List<SeatSelectionRequest> seats;

    // Lista de snacks seleccionados (opcional)
    @Valid
    private List<SnackSelectionRequest> snacks;
}
