package CinePacho.demo.payment.controller;

import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.PaymentWebhookRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;

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

    /**
     * Endpoint para testing manual del webhook de Stripe en Postman.
     * 
     * En producción, Stripe envía webhooks automáticamente.
     * Para testing manual, este endpoint permite simular la confirmación de pago.
     * 
     * ⚠️ IMPORTANTE: Este endpoint SOLO debe usarse en TESTING.
     * En producción, implementar webhook real de Stripe con verificación de firma.
     * 
     * Flujo:
     * 1. Usuario crea checkout → POST /api/checkout/stripe
     * 2. Usuario completa pago en Stripe (o simula aquí)
     * 3. Si exitoso → POST /api/checkout/stripe/webhook (ESTE endpoint)
     * 4. handlePaymentSuccess() ejecuta cambios de estado
     * 
     * @param request DTO con checkoutRequest, paymentId, y userEmail
     * @return 200 OK si el pago fue procesado exitosamente
     * @throws CinePachoException si la validación falla (usuario no coincide, sillas no bloqueadas, etc)
     */
    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> handlePaymentSuccessManual(
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        // Llamar al servicio con la información del webhook simulado
        stripeService.handlePaymentSuccess(
                request.getCheckoutRequest(),
                request.getPaymentId(),
                request.getUserEmail()
        );
        return ResponseEntity.ok("Payment processed successfully");
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
