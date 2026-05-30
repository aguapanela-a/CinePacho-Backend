package CinePacho.demo.snacks.controller;


import java.util.List;
import java.util.UUID;

import CinePacho.demo.snacks.dto.response.SnackByMultiplex;
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

    // Endpoint público para compradores: lista de snacks disponibles en un multiplex (solo admin)
    @GetMapping("/snacks/{multiplexId}")
    public ResponseEntity<List<SnackResponse>> getAvailable(
            @PathVariable UUID multiplexId
    ) {
        return ResponseEntity.ok(snackService.getAllAvailable(multiplexId));
    }

    //obtener todos los snacks de la BD central
    @GetMapping("/admin/snacks")
    public ResponseEntity<List<SnackByMultiplex>> getAll() {
        return ResponseEntity.ok(snackService.getAll());
    }

    //obtener snacks de un multiplex para managers
    @GetMapping("/admin/multiplexes/snacks")
    public ResponseEntity<List<SnackResponse>> getAllByMultiplex(){
        return ResponseEntity.ok(snackService.getAllByMultiplex());
    }
 
    @GetMapping("/admin/snacks/{id}")
    public ResponseEntity<SnackResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(snackService.getById(id));
    }
    
    //Status y mensaje de error o exito
    @PostMapping("/admin/snacks")
    public ResponseEntity<Void> create(@Valid @RequestBody SnackRequest request) {
        snackService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
 
    @PutMapping("/admin/snacks/{id}")
    public ResponseEntity<SnackResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SnackRequest request) {
        return ResponseEntity.ok(snackService.update(id, request));
    }
 
    @DeleteMapping("/admin/snacks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        snackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
