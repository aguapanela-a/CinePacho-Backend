package CinePacho.demo.reports.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Solicitud para generar reporte de ventas de snacks desde inicio del mes hasta la fecha enviada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSalesReportRequest {

    // Fecha final del reporte (inclusive).
    @NotNull
    private LocalDate endDate;
}
