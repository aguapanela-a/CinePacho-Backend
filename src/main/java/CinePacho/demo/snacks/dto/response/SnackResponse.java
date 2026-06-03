package CinePacho.demo.snacks.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackResponse {
 
    private UUID idSnack;
    private String nameSnack;
    private String descriptionSnack;
    private BigDecimal priceSnack;
    private int quantitySnack;
    private Boolean availableSnack;
    // Puntos asignados al snack
    private Integer pointsSnack;
    private UUID multiplexId;
}