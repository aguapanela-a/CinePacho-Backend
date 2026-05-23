package CinePacho.demo.payment.controller;

import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    public ResponseEntity<CheckoutSummaryResponse> preview(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token
    ) {
        // Calcula totales para vista previa
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(checkoutService.preview(request, token));
    }

    @PostMapping("/confirm")
    public ResponseEntity<CheckoutSummaryResponse> confirm(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token
    ) {
        // Revalida disponibilidad antes de continuar al pago
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(checkoutService.confirm(request, token));
    }
}
