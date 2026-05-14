package CinePacho.demo.snacks.controller;


import java.util.List;
import java.util.UUID;

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
 
    @GetMapping("admin/snacks")
    public ResponseEntity<List<SnackResponse>> getAll() {
        return ResponseEntity.ok(snackService.getAll());
    }
 
    @GetMapping("admin/snacks/{id}")
    public ResponseEntity<SnackResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(snackService.getById(id));
    }
 
    @PostMapping("admin/snacks")
    public ResponseEntity<SnackResponse> create(@Valid @RequestBody SnackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(snackService.create(request));
    }
 
    @PutMapping("admin/snacks/{id}")
    public ResponseEntity<SnackResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SnackRequest request) {
        return ResponseEntity.ok(snackService.update(id, request));
    }
 
    @DeleteMapping("admin/snacks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        snackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
