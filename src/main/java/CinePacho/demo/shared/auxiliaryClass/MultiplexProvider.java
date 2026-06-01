package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface MultiplexProvider {
    MultiplexEntity getMultiplexById(UUID id);
    // Retorna la lista de multiplexes (usado por módulos que necesitan listar multiplexes sin acoplarse al repositorio)
    List<MultiplexEntity> findAllMultiplexes();
}
