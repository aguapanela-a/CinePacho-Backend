package CinePacho.demo.rooms.dto.response;

import java.util.UUID;

import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
 
    private UUID idRoom;
    private Boolean isRoomActive;
    private String roomNumber;
}
 
