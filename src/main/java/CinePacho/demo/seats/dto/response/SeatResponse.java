package CinePacho.demo.seats.dto.response;

import CinePacho.demo.seats.enumeration.SeatStatus;
import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {
 
    private String idSeat;
    private String roomId;
    private Integer seatNumber;
    private String type;
    private SeatStatus status;
}
 