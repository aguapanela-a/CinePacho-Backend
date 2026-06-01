package CinePacho.demo.reports.controller;

import CinePacho.demo.reports.dto.request.SnackSalesReportRequest;
import CinePacho.demo.reports.dto.response.SnackSalesReportResponse;
import CinePacho.demo.reports.service.SnackSalesReportService;
import CinePacho.demo.reports.dto.response.MultiplexSnackSalesReport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador de reportes de ventas de snacks para administración.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class SnackSalesReportController {

    private final SnackSalesReportService snackSalesReportService;

    // Genera reporte de snacks desde inicio de mes hasta la fecha solicitada.
    @PostMapping("/snacks")
    public ResponseEntity<SnackSalesReportResponse> generateMonthlySnackSalesReport(
            @Valid @RequestBody SnackSalesReportRequest request
    ) {
        return ResponseEntity.ok(snackSalesReportService.buildMonthlySnackSalesReport(request.getEndDate()));
    }

    @GetMapping("/snacks/{multiplexId}/monthly")
    public ResponseEntity<MultiplexSnackSalesReport> getMonthlySnackSalesReportByMultiplex(
            @PathVariable UUID multiplexId,
            @RequestParam("endDate") LocalDate endDate
    ) {
        MultiplexSnackSalesReport report = snackSalesReportService.buildMonthlySnackSalesReportByMultiplex(multiplexId, endDate);
        return ResponseEntity.ok(report);
    }

}
