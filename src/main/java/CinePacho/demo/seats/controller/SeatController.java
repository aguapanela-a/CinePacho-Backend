package CinePacho.demo.seats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import CinePacho.demo.seats.service.SeatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class SeatController {
    
    private final SeatService seatService;

//     @PostMapping("")
//     public ResponseEntity<?> reserveSeat() {
//         seatService.verifyAvailability();
//         return ResponseEntity.ok().build();
//     }
}
