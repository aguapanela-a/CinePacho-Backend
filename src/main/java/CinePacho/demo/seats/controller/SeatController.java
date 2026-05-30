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

    @PutMapping("/seats/{seatId}/changeStatus") // Endpoint compartido para BUYER y EMPLOYEE
    public ResponseEntity<SeatResponse> changeState(
            @PathVariable UUID seatId,
            @RequestParam UUID screeningId,
            @RequestHeader("Authorization") String token
            ) {
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(seatService.toggleSeat(seatId,token, screeningId));
    }


    @GetMapping("/seats/{roomId}")
    public ResponseEntity<List<SeatResponse>>
    getAllByRoom(
            @PathVariable UUID roomId
    ) {
        return ResponseEntity.ok(seatService.getAllByRoom(roomId));
    }
}
