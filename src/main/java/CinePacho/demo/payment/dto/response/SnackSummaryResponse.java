package CinePacho.demo.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSummaryResponse {

    // Resumen de cada snack seleccionado
    private UUID snackId;
    private String nameSnack;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
