package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumen de ventas por función (screening) en un día.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningSalesReport {

    private UUID screeningId;
    private Long movieId;
    private String movieTitle;
    private Integer ticketsQuantity;
    private BigDecimal totalAmount;
}
