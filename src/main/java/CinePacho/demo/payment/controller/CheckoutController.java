package CinePacho.demo.payment.controller;

import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.StripeSuccessRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final StripeService stripeService;

    @PostMapping("/stripe")
    public ResponseEntity<CheckoutSummaryResponse> stripe(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token
    ) throws StripeException {
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(stripeService.checkoutProducts(request, token));
    }

    //redireccionamiento de stripe al completar el pago
    @PostMapping("/stripe/success")
    public ResponseEntity<Map<String, String>> success(
            @RequestBody StripeSuccessRequest request,
            @RequestHeader("Authorization") String token) {

        token = token.replace("Bearer ", "");
        Map<String, String> result = stripeService.handlePaymentSuccess(
                request.checkoutRequest(),
                request.paymentId(),
                token
        );
        return ResponseEntity.ok(result);
    }

    //redireccionamiento de stripe al cancelar el pago
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancel() {
        return ResponseEntity.ok(Map.of("message", "Pago cancelado"));
    }


    // @PostMapping("/preview")
    // public ResponseEntity<CheckoutSummaryResponse> preview(
    //         @Valid @RequestBody CheckoutRequest request,
    //         @RequestHeader("Authorization") String token
    // ) {
    //     // Calcula totales para vista previa
    //     token = token.replace("Bearer ", "");
    //     return ResponseEntity.ok(checkoutService.preview(request, token));
    // }

    // @PostMapping("/confirm")
    // public ResponseEntity<CheckoutSummaryResponse> confirm(
    //         @Valid @RequestBody CheckoutRequest request,
    //         @RequestHeader("Authorization") String token
    // ) {
    //     // Revalida disponibilidad antes de continuar al pago
    //     token = token.replace("Bearer ", "");
    //     return ResponseEntity.ok(checkoutService.confirm(request, token));
    // }
}
