package CinePacho.demo.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSummaryResponse {

    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;

    // ID del pago creado en backend - necesario para webhook de confirmación
    // Se utiliza en POST /api/checkout/stripe/webhook para confirmar el pago
    private UUID paymentId;

    // Multiplex asociado a las sillas seleccionadas
    private UUID multiplexId;

    // Totales calculados en backend
    private BigDecimal totalSeats;
    private BigDecimal totalSnacks;
    private BigDecimal totalPurchase;

    private List<SeatSummaryResponse> seats;
    private List<SnackSummaryResponse> snacks;

    
}
