package CinePacho.demo.payment.dto.response;

import CinePacho.demo.seats.enumeration.SeatStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatSummaryResponse {

    // Resumen de cada silla usada en el cálculo
    private UUID seatId;
    private String seatType;
    private SeatStatus seatStatus;
    private BigDecimal seatPrice;
}
