package CinePacho.demo.reports.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.reports.dto.response.DailySnackSalesReport;
import CinePacho.demo.reports.dto.response.MultiplexSnackSalesReport;
import CinePacho.demo.reports.dto.response.SnackSalesItemReport;
import CinePacho.demo.reports.dto.response.SnackSalesReportResponse;
import CinePacho.demo.reports.entities.SnackSaleEntity;
import CinePacho.demo.reports.repository.SnackSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio que genera el reporte de ventas de snacks desde inicio de mes hasta una fecha final.
 */
@Service
@RequiredArgsConstructor
public class SnackSalesReportService {

    private final SnackSaleRepository snackSaleRepository;
    private final MultiplexRepository multiplexRepository;

    /**
     * Construye el reporte agrupado por multiplex, día y snack.
     */
    @Transactional(readOnly = true)
    public SnackSalesReportResponse buildMonthlySnackSalesReport(LocalDate endDate) {
        if (endDate == null) {
            throw new CinePachoException("La fecha final del reporte es obligatoria");
        }

        LocalDate startDate = endDate.withDayOfMonth(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<SnackSaleEntity> sales = snackSaleRepository
                .findAllBySoldAtBetweenWithDetails(startDateTime, endDateTime);

        Map<UUID, Map<LocalDate, Map<UUID, AggregatedSnackSales>>> aggregated = aggregateSales(sales);
        List<LocalDate> days = buildDaysRange(startDate, endDate);

        List<MultiplexSnackSalesReport> multiplexReports = new ArrayList<>();
        for (MultiplexEntity multiplex : multiplexRepository.findAll()) {
            Map<LocalDate, Map<UUID, AggregatedSnackSales>> byDay = aggregated
                    .getOrDefault(multiplex.getId(), new HashMap<>());

            List<DailySnackSalesReport> dayReports = new ArrayList<>();
            for (LocalDate day : days) {
                Map<UUID, AggregatedSnackSales> snacks = byDay.getOrDefault(day, new HashMap<>());
                dayReports.add(DailySnackSalesReport.builder()
                        .date(day)
                        .snacks(toSnackReports(snacks))
                        .build());
            }

            multiplexReports.add(MultiplexSnackSalesReport.builder()
                    .multiplexId(multiplex.getId())
                    .multiplexName(multiplex.getName())
                    .days(dayReports)
                    .build());
        }

        return SnackSalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .multiplexes(multiplexReports)
                .build();
    }

    private Map<UUID, Map<LocalDate, Map<UUID, AggregatedSnackSales>>> aggregateSales(
            List<SnackSaleEntity> sales
    ) {
        // Estructura: multiplex -> día -> snack -> acumulado
        Map<UUID, Map<LocalDate, Map<UUID, AggregatedSnackSales>>> aggregated = new HashMap<>();

        for (SnackSaleEntity sale : sales) {
            UUID multiplexId = sale.getMultiplex().getId();
            LocalDate day = sale.getSoldAt().toLocalDate();
            UUID snackId = sale.getSnack().getId();

            aggregated
                    .computeIfAbsent(multiplexId, id -> new HashMap<>())
                    .computeIfAbsent(day, d -> new HashMap<>())
                    .computeIfAbsent(snackId, id -> new AggregatedSnackSales(sale))
                    .add(sale);
        }

        return aggregated;
    }

    private List<SnackSalesItemReport> toSnackReports(
            Map<UUID, AggregatedSnackSales> snacks
    ) {
        List<SnackSalesItemReport> reports = new ArrayList<>();
        for (AggregatedSnackSales aggregated : snacks.values()) {
            reports.add(aggregated.toReport());
        }
        return reports;
    }

    private List<LocalDate> buildDaysRange(LocalDate startDate, LocalDate endDate) {
        // Genera todos los días desde inicio de mes hasta la fecha solicitada.
        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            days.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    /**
     * Acumulador de ventas por snack para sumar cantidades y totales.
     */
    private static class AggregatedSnackSales {
        private final UUID snackId;
        private final String snackName;
        private int snacksQuantity;
        private BigDecimal totalAmount;

        private AggregatedSnackSales(SnackSaleEntity sale) {
            this.snackId = sale.getSnack().getId();
            this.snackName = sale.getSnack().getName();
            this.snacksQuantity = 0;
            this.totalAmount = BigDecimal.ZERO;
        }

        private void add(SnackSaleEntity sale) {
            this.snacksQuantity += sale.getSnacksQuantity();
            this.totalAmount = this.totalAmount.add(sale.getTotalAmount());
        }

        private SnackSalesItemReport toReport() {
            return SnackSalesItemReport.builder()
                    .snackId(snackId)
                    .snackName(snackName)
                    .snacksQuantity(snacksQuantity)
                    .totalAmount(totalAmount)
                    .build();
        }
    }
}
