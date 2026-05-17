package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface MultiplexProvider {
    MultiplexEntity obtenerMultiplexPorId(UUID id);
}
