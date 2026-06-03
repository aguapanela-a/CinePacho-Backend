package CinePacho.demo.snacks.controller;

import java.util.List;
import java.util.UUID;

import CinePacho.demo.snacks.dto.response.SnackByMultiplex;
import CinePacho.demo.shared.auxiliaryClass.DTOResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import CinePacho.demo.snacks.dto.request.SnackRequest;
import CinePacho.demo.snacks.dto.response.SnackResponse;
import CinePacho.demo.snacks.service.SnackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SnackController {
 
    private final SnackService snackService;

    // --- NUEVO: Endpoint público para obtener TODOS los snacks ---
    @GetMapping("/snacks/all")
    public ResponseEntity<List<SnackResponse>> getAllPublic() {
        return ResponseEntity.ok(snackService.findAllPublic());
    }

    // Endpoint público para compradores: lista de snacks disponibles en un multiplex
    @GetMapping("/snacks/{multiplexId}")
    public ResponseEntity<List<SnackResponse>> getAvailable(
            @PathVariable UUID multiplexId
    ) {
        return ResponseEntity.ok(snackService.getAllAvailable(multiplexId));
    }

    // obtener todos los snacks de la BD central (ADMIN)
    @GetMapping("/admin/snacks")
    public ResponseEntity<List<SnackByMultiplex>> getAll() {
        return ResponseEntity.ok(snackService.getAll());
    }

    // obtener snacks de un multiplex para managers (ADMIN y MANAGER)
    @GetMapping("/admin/multiplexes/{multiplexId}/snacks")
    public ResponseEntity<List<SnackResponse>> getAllByMultiplex(
            @PathVariable UUID multiplexId
    ){
        return ResponseEntity.ok(snackService.getAllByMultiplex(multiplexId));
    }
 
    @GetMapping("/admin/snacks/{id}")
    public ResponseEntity<SnackResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(snackService.getById(id));
    }
    
    // Status y mensaje de error o exito
    @PostMapping("/admin/snacks")
    public ResponseEntity<DTOResponse> create(@Valid @RequestBody SnackRequest request) {
        snackService.create(request);
        // Respuesta estándar para creación de snack
        DTOResponse response = DTOResponse.withStatus(
                "Snack creado con éxito",
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PutMapping("/admin/snacks/{id}")
    public ResponseEntity<SnackResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SnackRequest request) {
        return ResponseEntity.ok(snackService.update(id, request));
    }
 
    @DeleteMapping("/admin/snacks/{id}")
    public ResponseEntity<DTOResponse> delete(@PathVariable UUID id) {
        snackService.delete(id);
        // Respuesta estándar para eliminación de snack
        DTOResponse response = DTOResponse.withStatus(
                "Snack eliminado con éxito",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}