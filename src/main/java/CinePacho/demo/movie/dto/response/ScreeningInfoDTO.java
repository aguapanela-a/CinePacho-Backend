package CinePacho.demo.movie.dto.response;

import CinePacho.demo.movie.enumeration.ScreeningStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;
@Builder
public record ScreeningInfoDTO(
        UUID screeningId,
        UUID roomId,
        String roomNumber,
        LocalDateTime screeningDate,
        ScreeningStatus status
) {
}
