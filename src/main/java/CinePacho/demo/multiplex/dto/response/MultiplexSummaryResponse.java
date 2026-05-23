package CinePacho.demo.multiplex.dto.response;

import lombok.*;
import java.math.BigDecimal;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexSummaryResponse {
 
    private String idMultiplex;
    private String nameMultiplex;
    private String cityMultiplex;

    // Precios actuales por tipo de silla (útiles para listas administrativas)
    private BigDecimal generalSeatPrice;
    private BigDecimal preferentialSeatPrice;
}
