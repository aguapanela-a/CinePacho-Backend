package CinePacho.demo.seats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAvailabilitySummaryResponse {

    private Integer availableGeneral;
    private Integer availablePreferential;
    private Integer totalAvailable;
    
}
