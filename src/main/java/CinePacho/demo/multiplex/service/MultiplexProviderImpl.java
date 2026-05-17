package CinePacho.demo.multiplex.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MultiplexProviderImpl implements MultiplexProvider {
    private final MultiplexRepository multiplexRepository;

    public MultiplexProviderImpl(MultiplexRepository multiplexRepository) {
        this.multiplexRepository = multiplexRepository;
    }

    @Override
    public MultiplexEntity obtenerMultiplexPorId(UUID id) {
        return multiplexRepository.findById(id).orElseThrow(()-> new CinePachoException("El ID del múltiplex no existe"));
    }
}
