package CinePacho.demo.multiplex.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import CinePacho.demo.multiplex.dto.request.MultiplexRequest;
import CinePacho.demo.multiplex.dto.response.MultiplexDetailResponse;
import CinePacho.demo.multiplex.dto.response.MultiplexSummaryResponse;
import CinePacho.demo.multiplex.service.MultiplexService;

import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MultiplexController {
 
    private final MultiplexService multiplexService;
 
    @GetMapping("/admin/multiplexes")
    public ResponseEntity<List<MultiplexSummaryResponse>> getAll() {
        return ResponseEntity.ok(multiplexService.getAll());
    }
 
    @GetMapping("/admin/multiplexes/{id}")
    public ResponseEntity<MultiplexDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(multiplexService.getById(id));
    }
 
    @PostMapping("/admin/multiplexes")
    public ResponseEntity<MultiplexDetailResponse> create(@Valid @RequestBody MultiplexRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(multiplexService.create(request));
    }
 
    @PutMapping("/admin/multiplexes/{id}")
    public ResponseEntity<MultiplexDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MultiplexRequest request) {
        return ResponseEntity.ok(multiplexService.update(id, request));
    }
 
    @DeleteMapping("/admin/multiplexes/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        multiplexService.delete(id);
        return ResponseEntity.noContent().build();
    }
}