package CinePacho.demo.snacks.dto.response;

import lombok.*;
 
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
    private double priceSnack;
    private int quantitySnack;
}