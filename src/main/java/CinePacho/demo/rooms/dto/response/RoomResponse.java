package CinePacho.demo.rooms.dto.response;

import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
 
    private String idRoom;
    private Boolean isRoomActive;
}
 
