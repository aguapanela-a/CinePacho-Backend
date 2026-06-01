package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Reporte de ventas de snacks agrupado por multiplex.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexSnackSalesReport {

    private UUID multiplexId;
    private String multiplexName;
    private List<DailySnackSalesReport> days;
}
