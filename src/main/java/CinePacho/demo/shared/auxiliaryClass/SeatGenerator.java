package CinePacho.demo.shared.auxiliaryClass;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface SeatGenerator {
    void createSeat(int general, int preferential, UUID roomId);
}
