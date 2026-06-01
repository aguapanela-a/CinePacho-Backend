package CinePacho.demo.payment.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
@Builder
public record BillingDTO(
        String status,
        String message,
        BigDecimal totalSeats,
        BigDecimal totalSnacks,
        BigDecimal totalPurchase,
        List<SeatSummaryResponse> seats,
        List<SnackSummaryResponse> snacks,
        String roomNumber,
        List<Integer> seatsNumbers,
        String movieTitle,
        String screeningDate
) {}
