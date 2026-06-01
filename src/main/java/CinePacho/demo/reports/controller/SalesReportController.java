package CinePacho.demo.reports.controller;

import CinePacho.demo.reports.dto.request.SalesReportRequest;
import CinePacho.demo.reports.dto.response.MultiplexSalesReport;
import CinePacho.demo.reports.dto.response.SalesReportResponse;
import CinePacho.demo.reports.service.SalesReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador de reportes de ventas para administración.
 */
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


    @GetMapping("/sales/{multiplexId}")
    public ResponseEntity<MultiplexSalesReport> getMonthlySalesReportByMultiplex(
            @PathVariable UUID multiplexId,
            @RequestParam("endDate") LocalDate endDate
    ) {
        MultiplexSalesReport report = salesReportService.buildMonthlySalesReportByMultiplex(multiplexId, endDate);
        return ResponseEntity.ok(report);
    }
}
