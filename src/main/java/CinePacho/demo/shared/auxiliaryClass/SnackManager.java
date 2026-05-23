package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.snacks.entities.SnackEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface SnackManager {
    // Encapsula el acceso a snacks para otros módulos sin exponer repositorios
    List<SnackEntity> findAllById(List<UUID> ids);
}
