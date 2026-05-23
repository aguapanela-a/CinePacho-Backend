package CinePacho.demo.seats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import CinePacho.demo.seats.dto.response.SeatResponse;
import CinePacho.demo.seats.service.SeatService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class SeatController {
    
    private final SeatService seatService;

    @PutMapping("/admin/seats/changeStatus/{seatId}")
    public ResponseEntity<SeatResponse> changeState(@PathVariable UUID seatId) {
        return ResponseEntity.ok(seatService.changeState(seatId));
    }

//     @PostMapping("")
//     public ResponseEntity<?> reserveSeat() {
//         seatService.verifyAvailability();
//         return ResponseEntity.ok().build();
//     }
}
