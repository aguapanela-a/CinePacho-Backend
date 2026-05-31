package CinePacho.demo.payment.dto.request;

import java.util.UUID;

public record StripeSuccessRequest (
        UUID paymentId,
        CheckoutRequest checkoutRequest
){
}
