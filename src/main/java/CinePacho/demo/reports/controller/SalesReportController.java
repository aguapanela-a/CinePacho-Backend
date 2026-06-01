package CinePacho.demo.reports.controller;

import CinePacho.demo.reports.dto.request.SalesReportRequest;
import CinePacho.demo.reports.dto.response.SalesReportResponse;
import CinePacho.demo.reports.service.SalesReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de reportes de ventas para administración.
 */

// TODO:  hacer filtro de reportes por multiplex id
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;

    // Genera reporte desde inicio de mes hasta la fecha solicitada.
    @PostMapping("/sales")
    public ResponseEntity<SalesReportResponse> generateMonthlySalesReport(
            @Valid @RequestBody SalesReportRequest request
    ) {
        return ResponseEntity.ok(salesReportService.buildMonthlySalesReport(request.getEndDate()));
    }
}
