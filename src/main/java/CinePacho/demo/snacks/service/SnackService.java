package CinePacho.demo.snacks.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
import org.springframework.stereotype.Service;

import CinePacho.demo.snacks.dto.request.SnackRequest;
import CinePacho.demo.snacks.dto.response.SnackResponse;
import CinePacho.demo.snacks.entities.SnackEntity;
import CinePacho.demo.snacks.repository.SnackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class SnackService {
 
    private final SnackRepository snackRepository;
    private final AccessValidator accessValidator;
    private final MultiplexProvider multiplexProvider;

    //Obtener todos los snacks
    public List<SnackResponse> getAll() {

        return snackRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lista snacks disponibles para compra (cantidad > 0) en un multiplex
    public List<SnackResponse> getAllAvailable(UUID multiplexId) {


        return snackRepository.findByQuantityGreaterThanAndMultiplex_Id(0, multiplexId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    public SnackResponse getById(UUID id) {
        SnackEntity snack = snackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Snack no encontrado con id: " + id));
        return toResponse(snack);
    }
 
    public SnackResponse create(SnackRequest request) {
        SnackEntity snack = SnackEntity.builder()
                .name(request.getNameSnack())
                .description(request.getDescriptionSnack())
                .price(request.getPriceSnack())
                .quantity(request.getQuantitySnack())
                .multiplex(multiplexProvider.getMultiplexById(request.getMultiplexId()))
                .build();
        return toResponse(snackRepository.save(snack));
    }
 
    public SnackResponse update(UUID id, SnackRequest request) {
        SnackEntity snack = snackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Snack no encontrado con id: " + id));
 
        snack.setName(request.getNameSnack());
        snack.setDescription(request.getDescriptionSnack());
        snack.setPrice(request.getPriceSnack());
        snack.setQuantity(request.getQuantitySnack());
        return toResponse(snackRepository.save(snack));
    }
 
    public void delete(UUID id) {
        if (!snackRepository.existsById(id)) {
            throw new EntityNotFoundException("Snack no encontrado con id: " + id);
        }
        snackRepository.deleteById(id);
    }
 
    // ── Mapper interno ──────────────────────────────────────────────────────────
    private SnackResponse toResponse(SnackEntity snack) {
        return SnackResponse.builder()
                .idSnack(snack.getId())
                .nameSnack(snack.getName())
                .descriptionSnack(snack.getDescription())
                .priceSnack(snack.getPrice())
                .quantitySnack(snack.getQuantity())
                .build();
    }
}
