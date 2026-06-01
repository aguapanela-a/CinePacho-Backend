package CinePacho.demo.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO para recibir solicitudes manuales del webhook de Stripe en testing.
 * 
 * En producción, Stripe envía el webhook directamente, pero para testing manual
 * en Postman, necesitamos un endpoint que reciba esta información.
 * 
 * ⚠️ IMPORTANTE: Este DTO es SOLO para testing. En producción, el webhook real
 * viene de Stripe y debe ser procesado de manera diferente (verificando firma).
 * 
 * Estructura:
 * - checkoutRequest: La información del checkout original
 * - paymentId: ID del pago que fue creado en checkoutProducts()
 * - userEmail: Email del usuario que realizó la compra (extraído del JWT)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {

    // La solicitud de checkout original con sillas, snacks, y screening
    private CheckoutRequest checkoutRequest;

    // ID del pago que debe ser actualizado a COMPLETED
    private UUID paymentId;

    // Email del usuario que realizó el checkout (para validar que bloqueó las sillas)
    private String userEmail;
}
