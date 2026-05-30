package CinePacho.demo.snacks.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
import CinePacho.demo.snacks.dto.response.SnackByMultiplex;
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

    //Obtener los snacks de la BD central listados por multiplex (SOLO ADMIN)
    public List<SnackByMultiplex> getAll() {
        return toSnackByMultiplexList(snackRepository.findAll());
    }

    //Obtener los snaks de un multiplex (SOLO EMPLEADO)
    public List<SnackResponse> getAllByMultiplex(){
        UUID multiplexId = accessValidator.getScopedMultiplexIdForAdminOrManager();
        accessValidator.validateMultiplexAccess(multiplexId);
        List<SnackEntity> snacksByMultiplex = snackRepository.findAllByMultiplex_Id(multiplexId);

        return snacksByMultiplex.stream().map(this::toResponse).collect(Collectors.toList());
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

        //validar que si es manager solo pueda crear snacks en su multiplex
        accessValidator.validateMultiplexAccess(request.getMultiplexId());

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
                .availableSnack(snack.getQuantity() > 0)
                .quantitySnack(snack.getQuantity())
                .build();
    }

    private List<SnackByMultiplex> toSnackByMultiplexList(List<SnackEntity> snack) {

        if (snack.isEmpty()) {
            return null;
        }

        List<SnackByMultiplex> snackByMultiplexList = new ArrayList<>();

        Map<MultiplexEntity, List<SnackEntity>> snacksByMultiplex = snack.stream()
                .collect(Collectors.groupingBy(SnackEntity::getMultiplex));

        snacksByMultiplex.forEach((multiplex, snacks) -> {
            List<SnackResponse> snackResponses = snacks.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());


            SnackByMultiplex snackByMultiplex = SnackByMultiplex.builder()
                    .multiplexName(multiplex.getName())
                    .snacks(snackResponses)
                    .build();

            snackByMultiplexList.add(snackByMultiplex);
        });

        return snackByMultiplexList;
    }
}
