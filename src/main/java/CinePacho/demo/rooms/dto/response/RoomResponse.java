package CinePacho.demo.rooms.dto.response;

import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
 
    private String idSala;
    private Integer generalCapacity;
    private Integer preferentialCapacity;
    private Boolean isSalaActive;
}
 
