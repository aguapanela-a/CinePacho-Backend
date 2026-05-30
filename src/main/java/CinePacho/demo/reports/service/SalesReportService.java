package CinePacho.demo.reports.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.reports.dto.response.DailySalesReport;
import CinePacho.demo.reports.dto.response.MultiplexSalesReport;
import CinePacho.demo.reports.dto.response.SalesReportResponse;
import CinePacho.demo.reports.dto.response.ScreeningSalesReport;
import CinePacho.demo.reports.entities.TicketSaleEntity;
import CinePacho.demo.reports.repository.TicketSaleRepository;
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
 * Servicio que genera el reporte de ventas de tickets desde inicio de mes hasta una fecha final.
 */
@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final TicketSaleRepository ticketSaleRepository;
    private final MultiplexRepository multiplexRepository;

    /**
     * Construye el reporte agrupado por multiplex, día y función.
     */
    @Transactional(readOnly = true)
    public SalesReportResponse buildMonthlySalesReport(LocalDate endDate) {
        if (endDate == null) {
            throw new CinePachoException("La fecha final del reporte es obligatoria");
        }

        LocalDate startDate = endDate.withDayOfMonth(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<TicketSaleEntity> sales = ticketSaleRepository
                .findAllBySoldAtBetweenWithDetails(startDateTime, endDateTime);

        Map<UUID, Map<LocalDate, Map<UUID, AggregatedScreeningSales>>> aggregated = aggregateSales(sales);
        List<LocalDate> days = buildDaysRange(startDate, endDate);

        List<MultiplexSalesReport> multiplexReports = new ArrayList<>();
        for (MultiplexEntity multiplex : multiplexRepository.findAll()) {
            Map<LocalDate, Map<UUID, AggregatedScreeningSales>> byDay = aggregated
                    .getOrDefault(multiplex.getId(), new HashMap<>());

            List<DailySalesReport> dayReports = new ArrayList<>();
            for (LocalDate day : days) {
                Map<UUID, AggregatedScreeningSales> screenings = byDay.getOrDefault(day, new HashMap<>());
                dayReports.add(DailySalesReport.builder()
                        .date(day)
                        .screenings(toScreeningReports(screenings))
                        .build());
            }

            multiplexReports.add(MultiplexSalesReport.builder()
                    .multiplexId(multiplex.getId())
                    .multiplexName(multiplex.getName())
                    .days(dayReports)
                    .build());
        }

        return SalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .multiplexes(multiplexReports)
                .build();
    }

    private Map<UUID, Map<LocalDate, Map<UUID, AggregatedScreeningSales>>> aggregateSales(
            List<TicketSaleEntity> sales
    ) {
        // Estructura: multiplex -> día -> screening -> acumulado
        Map<UUID, Map<LocalDate, Map<UUID, AggregatedScreeningSales>>> aggregated = new HashMap<>();

        for (TicketSaleEntity sale : sales) {
            UUID multiplexId = sale.getMultiplex().getId();
            LocalDate day = sale.getSoldAt().toLocalDate();
            UUID screeningId = sale.getScreening().getId();

            aggregated
                    .computeIfAbsent(multiplexId, id -> new HashMap<>())
                    .computeIfAbsent(day, d -> new HashMap<>())
                    .computeIfAbsent(screeningId, id -> new AggregatedScreeningSales(sale))
                    .add(sale);
        }

        return aggregated;
    }

    private List<ScreeningSalesReport> toScreeningReports(
            Map<UUID, AggregatedScreeningSales> screenings
    ) {
        List<ScreeningSalesReport> reports = new ArrayList<>();
        for (AggregatedScreeningSales aggregated : screenings.values()) {
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
     * Acumulador de ventas por función para sumar cantidades y totales.
     */
    private static class AggregatedScreeningSales {
        private final UUID screeningId;
        private final Long movieId;
        private final String movieTitle;
        private int ticketsQuantity;
        private BigDecimal totalAmount;

        private AggregatedScreeningSales(TicketSaleEntity sale) {
            this.screeningId = sale.getScreening().getId();
            this.movieId = sale.getScreening().getMovie().getId();
            this.movieTitle = sale.getScreening().getMovie().getOriginalTitle();
            this.ticketsQuantity = 0;
            this.totalAmount = BigDecimal.ZERO;
        }

        private void add(TicketSaleEntity sale) {
            this.ticketsQuantity += sale.getTicketsQuantity();
            this.totalAmount = this.totalAmount.add(sale.getTotalAmount());
        }

        private ScreeningSalesReport toReport() {
            return ScreeningSalesReport.builder()
                    .screeningId(screeningId)
                    .movieId(movieId)
                    .movieTitle(movieTitle)
                    .ticketsQuantity(ticketsQuantity)
                    .totalAmount(totalAmount)
                    .build();
        }
    }
}
