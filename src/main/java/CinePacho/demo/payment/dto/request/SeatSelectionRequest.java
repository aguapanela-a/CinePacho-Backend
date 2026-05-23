package CinePacho.demo.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatSelectionRequest {

    // ID de la silla seleccionada
    @NotNull(message = "El id de la silla es obligatorio")
    private UUID seatId;
}
