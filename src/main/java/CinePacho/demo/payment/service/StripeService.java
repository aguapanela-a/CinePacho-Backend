package CinePacho.demo.payment.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StripeService {

    private final CheckoutService checkoutService;
    private final PaymentRepository paymentRepository;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // ¿Está esto local?
    // teaer url.base del backend
    @Value("${app.base-url}")
    private String BASE_URL;

    //TODO: cambiar esto para que tome el valor de la variable entorno
    private static final String SUCCESS_URL = "http://localhost:8010/api/checkout/stripe/success";
    private static final String CANCEL_URL = "http://localhost:8010/api/checkout/stripe/cancel";
    private static final String CURRENCY = "COP";

    @Autowired
    public StripeService(CheckoutService checkoutService, PaymentRepository paymentRepository, BuyerManager buyerManager, MovieManager movieManager) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
    }

    /**
     * Procesa la compra de los productos (sillas y snacks) llamando a Stripe para generar una sesión de pago.
     * @param request Datos de la solicitud de checkout incluyendo sillas, snacks y el ID de la función.
     * @param token Token JWT del usuario para extraer su información.
     * @return Resumen del checkout con la URL de la sesión de Stripe para redirigir al usuario.
     */
    public CheckoutSummaryResponse checkoutProducts(CheckoutRequest request, String token) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        // Confirma disponibilidad y calcula el resumen del pago con CheckoutService
        CheckoutSummaryResponse summary = checkoutService.confirm(request, token);

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        List<SeatSummaryResponse> seats = summary.getSeats() == null
                ? Collections.emptyList()
                : summary.getSeats();
        for (SeatSummaryResponse seat : seats) {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(CURRENCY)
                            .setUnitAmount(toStripeAmount(seat.getSeatPrice().multiply(BigDecimal.valueOf(100))))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Silla " + seat.getSeatType())
                                    .build())
                            .build())
                    .build();
            lineItems.add(lineItem);
        }

        List<SnackSummaryResponse> snacks = summary.getSnacks() == null
                ? Collections.emptyList()
                : summary.getSnacks();
        for (SnackSummaryResponse snack : snacks) {
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(snack.getQuantity().longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(CURRENCY)
                            .setUnitAmount(toStripeAmount(snack.getUnitPrice().multiply(BigDecimal.valueOf(100))))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(snack.getNameSnack())
                                    .build())
                            .build())
                    .build();
            lineItems.add(lineItem);
        }

        if (lineItems.isEmpty()) {
            throw new CinePachoException("No hay productos para enviar al checkout de Stripe");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL)
                .setCancelUrl(CANCEL_URL)
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);

        // Se crea el registro del pago en estado PENDING, que luego cambiará a SUCCESS o FAILED
        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(checkoutService.getUserIdFromToken(token)); //Falta completar metodo
        payment.setAmount(summary.getTotalPurchase());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.PENDING);  //modificar para que se actualice a SUCCESS o FAILED según corresponda después de la confirmación del pago en Stripe

        // Aquí se guarda el payment en la base de datos 
        paymentRepository.save(payment);


        summary.setStatus("SUCCESS");
        summary.setMessage("Checkout creado correctamente");
        summary.setSessionId(session.getId());
        summary.setSessionUrl(session.getUrl());

        //TODO: Crear entidad para el histórico de películas vistas
        // Agregando la película a las vistas del buyer usando interfaces para no acoplar módulos
        if (summary.getStatus().equals("SUCCESS") && payment.getUserId() != null) {
            Long movieId = movieManager.getMovieIdByScreeningId(request.getScreeningId());
            buyerManager.addWatchedMovie(payment.getUserId(), movieId);
        }

        return summary;
    }

    /**
     * Convierte el monto decimal al formato entero en centavos requerido por Stripe.
     * @param amount Monto en BigDecimal.
     * @return Monto convertido a long en centavos.
     */
    private long toStripeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new CinePachoException("El precio del producto no puede ser nulo");
        }
        return amount.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
