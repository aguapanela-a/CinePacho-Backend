package CinePacho.demo.movie.dto.response;
 
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
 
@Builder
public record ScreeningInfoDTO(
        UUID screeningId,
        UUID roomId,
        String roomNumber,
        @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
        LocalDateTime screeningDate,
        ScreeningStatus status,
        String format,
        BigDecimal generalPrice,
        BigDecimal preferentialPrice
) {
}