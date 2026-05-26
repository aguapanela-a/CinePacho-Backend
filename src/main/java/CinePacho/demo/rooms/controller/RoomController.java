package CinePacho.demo.rooms.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.service.RoomService;

import java.util.UUID;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {
 
    private final RoomService roomService;
    
    // @GetMapping("admin/rooms/{id}")
    // public ResponseEntity<RoomDetailResponse> getById(@PathVariable UUID id) {
    //     return ResponseEntity.ok(roomService.getById(id));
    // }



    @PostMapping("admin/{multiplexId}/rooms")
    public ResponseEntity<ResponseSummary> create(@Valid @PathVariable UUID multiplexId) {

        RoomDetailResponse detail = roomService.create(multiplexId);
        // Se elimina la segunda creación para evitar duplicar salas

        return ResponseEntity.ok(new ResponseSummary("Sala de cine creada con éxito", detail.getIdRoom()));
    }
 
    @DeleteMapping("admin/rooms/{id}")
    public ResponseEntity<ResponseSummary> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.ok(new ResponseSummary("Sala de cine eliminada con éxito", id));
    }

    public record ResponseSummary(
            String message,
            @NotBlank(message = "El id de la sala es requerido")
            UUID roomId
    ) {}
}
