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

    @PutMapping("/seats/reserveSeat/{seatId}")
    public ResponseEntity<SeatResponse> changeState(
            @PathVariable UUID seatId,
            @RequestHeader("Authorization") String token
            ) {
        return ResponseEntity.ok(seatService.toggleSeat(seatId,token));
    }


    @GetMapping("/seats")
    public ResponseEntity<List<SeatResponse>> getAllByRoom(UUID roomId) {
        return ResponseEntity.ok(seatService.getAllByRoom(roomId));
    }
}
