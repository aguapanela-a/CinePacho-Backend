package CinePacho.demo.snacks.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
 
    public List<SnackResponse> getAll() {
        return snackRepository.findAll()
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
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        return toResponse(snackRepository.save(snack));
    }
 
    public SnackResponse update(UUID id, SnackRequest request) {
        SnackEntity snack = snackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Snack no encontrado con id: " + id));
 
        snack.setName(request.getName());
        snack.setDescription(request.getDescription());
        snack.setPrice(request.getPrice());
        snack.setQuantity(request.getQuantity());
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
