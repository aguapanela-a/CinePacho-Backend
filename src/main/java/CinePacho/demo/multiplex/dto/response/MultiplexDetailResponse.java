package CinePacho.demo.multiplex.dto.response;

import lombok.*;
 
import java.util.List;

import CinePacho.demo.rooms.dto.response.RoomResponse;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexDetailResponse {
 
    private String idMultiplex;
    private String nameMultiplex;
    private String addressMultiplex;
    private String cityMultiplex;
    private List<RoomResponse> rooms;
}
 