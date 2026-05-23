package CinePacho.demo.snacks.service;

import CinePacho.demo.shared.auxiliaryClass.SnackManager;
import CinePacho.demo.snacks.entities.SnackEntity;
import CinePacho.demo.snacks.repository.SnackRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SnackManagerImpl implements SnackManager {

    private final SnackRepository snackRepository;

    public SnackManagerImpl(SnackRepository snackRepository) {
        this.snackRepository = snackRepository;
    }

    @Override
    public List<SnackEntity> findAllById(List<UUID> ids) {
        // Centraliza la consulta de snacks para módulos externos
        return snackRepository.findAllById(ids);
    }
}
