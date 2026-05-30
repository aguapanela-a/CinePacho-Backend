package CinePacho.demo.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte diario de ventas de snacks.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySnackSalesReport {

    private LocalDate date;
    private List<SnackSalesItemReport> snacks;
}
