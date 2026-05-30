package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Agrupa ventas por multiplex dentro del rango solicitado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexSalesReport {

    private UUID multiplexId;
    private String multiplexName;
    private List<DailySalesReport> days;
}
