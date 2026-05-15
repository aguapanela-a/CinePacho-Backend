package CinePacho.demo.multiplex.dto.response;

import lombok.*;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexSummaryResponse {
 
    private String idMultiplex;
    private String nameMultiplex;
    private String cityMultiplex;
}
