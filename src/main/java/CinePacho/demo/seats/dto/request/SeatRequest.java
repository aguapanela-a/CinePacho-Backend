package CinePacho.demo.seats.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
 
import java.util.UUID;

import CinePacho.demo.shared.enumeration.SeatType;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRequest {
 
    @NotNull(message = "El id de la sala es obligatorio")
    private UUID roomId;
 
    @NotNull(message = "El número de asiento es obligatorio")
    @Positive(message = "El número de asiento debe ser mayor a 0")
    private Integer seatNumber;
 
    @NotNull(message = "El tipo de asiento es obligatorio (GENERAL o PREFERENTIAL)")
    private SeatType type;
}