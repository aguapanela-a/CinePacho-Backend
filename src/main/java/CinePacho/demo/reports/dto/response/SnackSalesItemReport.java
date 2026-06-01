package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumen de ventas por snack para un día específico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSalesItemReport {

    private UUID snackId;
    private String snackName;
    private Integer snacksQuantity;
    private BigDecimal totalAmount;
}
