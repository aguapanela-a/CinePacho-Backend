package CinePacho.demo.seats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import CinePacho.demo.seats.dto.response.SeatResponse;
import CinePacho.demo.seats.service.SeatService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PutMapping("/seats/{seatId}/screening/{screeningId}/changeStatus") // Endpoint compartido para BUYER y EMPLOYEE
    public ResponseEntity<SeatResponse> changeState(
            @PathVariable UUID seatId,
            @PathVariable UUID screeningId,
            @RequestHeader("Authorization") String token
            ) {
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(seatService.toggleSeat(seatId, token, screeningId));
    }


    @GetMapping("/seats/{roomId}/screening/{screeningId}")
    public ResponseEntity<List<SeatResponse>>
    getAllByRoom(
            @PathVariable UUID roomId,
            @PathVariable UUID screeningId
    ) {
        return ResponseEntity.ok(seatService.getAllByRoom(roomId, screeningId));
    }
}
