package CinePacho.demo.rooms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import CinePacho.demo.rooms.dto.request.RoomRequest;
import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.service.RoomService;
import CinePacho.demo.rooms.dto.response.RoomResponse;

import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class RoomController {
 
    private final RoomService roomService;
 
    
    @GetMapping("/admin/rooms")
    public ResponseEntity<List<RoomResponse>> getAll() {
        return ResponseEntity.ok(roomService.getAll());
    }
 
    
    @GetMapping("admin/rooms/{id}")
    public ResponseEntity<RoomDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @PostMapping("admin/rooms")
    public ResponseEntity<Void> create(@Valid @RequestBody RoomRequest request) {

        roomService.create(request);
        return ResponseEntity.ok().build();
    }
 
    @DeleteMapping("admin/rooms/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}