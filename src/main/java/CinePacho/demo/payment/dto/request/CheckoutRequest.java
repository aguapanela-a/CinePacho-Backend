package CinePacho.demo.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    // Lista de sillas seleccionadas (obligatoria)
    @NotEmpty(message = "Debe seleccionar al menos una silla")
    @Valid
    private List<SeatSelectionRequest> seats;

    // Lista de snacks seleccionados (opcional)
    @Valid
    private List<SnackSelectionRequest> snacks;
}
