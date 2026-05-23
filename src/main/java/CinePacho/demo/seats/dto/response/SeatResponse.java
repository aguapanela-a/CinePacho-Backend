package CinePacho.demo.seats.dto.response;

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
    private boolean isAvailable;
}
 