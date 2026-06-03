package CinePacho.demo.multiplex.dto.response;

import lombok.*;
import java.math.BigDecimal;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexSummaryResponse {
 
    private String idMultiplex;
    private String nameMultiplex;
    private String cityMultiplex;
    private long numberOfRooms;
    private String addressMultiplex;
    private BigDecimal generalSeatPrice;
    private BigDecimal preferentialSeatPrice;
}
