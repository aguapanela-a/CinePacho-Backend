package CinePacho.demo.rooms.dto.response;

import lombok.*;
 
import java.util.List;

import CinePacho.demo.seats.dto.response.SeatAvailabilitySummaryResponse;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailResponse {
 
    private String idSala;
    private String multiplexId;
    private Integer numberRoom;
    private Integer generalCapacity;
    private Integer preferentialCapacity;
    private Boolean isSalaActive;
    private List<SeatAvailabilitySummaryResponse> seats;  
}