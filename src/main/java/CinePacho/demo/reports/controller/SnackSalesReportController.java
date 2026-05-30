package CinePacho.demo.reports.controller;

import CinePacho.demo.reports.dto.request.SnackSalesReportRequest;
import CinePacho.demo.reports.dto.response.SnackSalesReportResponse;
import CinePacho.demo.reports.service.SnackSalesReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
