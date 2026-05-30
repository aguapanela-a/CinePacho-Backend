package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta raíz del reporte mensual de ventas de snacks.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSalesReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<MultiplexSnackSalesReport> multiplexes;
}
