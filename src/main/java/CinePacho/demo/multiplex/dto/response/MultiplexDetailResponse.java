package CinePacho.demo.multiplex.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;
 
import java.util.List;

import CinePacho.demo.rooms.dto.response.RoomResponse;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexDetailResponse {

    @NotNull(message = "el id del multiplex no debe ser nulo")
    private String idMultiplex;
    private String nameMultiplex;
    private String addressMultiplex;
    private String cityMultiplex;
    private List<RoomResponse> rooms;
}
 