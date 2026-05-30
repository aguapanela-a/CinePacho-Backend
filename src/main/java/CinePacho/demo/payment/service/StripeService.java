package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.serviceSecurity.JwtService;
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
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SeatManager seatManager;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CURRENCY = "COP";

    @Autowired
    public StripeService(
            CheckoutService checkoutService,
            PaymentRepository paymentRepository,
            BuyerManager buyerManager,
            MovieManager movieManager,
            JwtService jwtService,
            UserManager userManager, SeatManager seatManager
    ) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
        this.seatManager = seatManager;
    }

    public CheckoutSummaryResponse checkoutProducts(CheckoutRequest request, String token) throws StripeException {


        Stripe.apiKey = stripeApiKey;

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

        // construye las URLs aquí donde baseUrl ya está inyectado
        String successUrl = baseUrl + "/api/checkout/stripe/success";
        String cancelUrl = baseUrl + "/api/checkout/stripe/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);

        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(checkoutService.getUserIdFromToken(token));
        payment.setAmount(summary.getTotalPurchase());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        summary.setStatus("SUCCESS");
        summary.setMessage("Checkout creado correctamente");
        summary.setSessionId(session.getId());
        summary.setSessionUrl(session.getUrl());

        //añadir película al historuiial de pelis vistas por el usuario
        registerWatchedMovieForBuyer(token, request, summary, payment);

        //cambiar el estado de las sillas a vendidas
        request.getSeats().stream()
                .forEach(
                        seatsIDs -> seatManager.updateSeatStatus(seatsIDs.getSeatId(), SeatStatus.SOLD)
                );

        //inicializo el timer para liberar las sillas en 3 horas para la funcion de la request
        MovieScreening screening = movieManager.getMovieScreeningById(request.getScreeningId());

        seatManager.scheduleRelease(
                screening.getId(),
                screening.getRoom().getId(),
                screening.getDateTime()
        );

        return summary;
    }



    private void registerWatchedMovieForBuyer(
            String token,
            CheckoutRequest request,
            CheckoutSummaryResponse summary,
            PaymentEntity payment
    ) {
        String userEmail = jwtService.extractEmail(token);
        UserEntity currentUser = userManager.getUserByEmail(userEmail);

        if (currentUser.getUserType() != UserType.BUYER || !summary.getStatus().equals("SUCCESS") || payment.getUserId() == null) {
            throw new CinePachoException("El usuario no es un comprador o el pago no fue exitoso");

        }

        BuyerEntity buyer = buyerManager.getBuyerByEmail(userEmail);
        Long movieId = movieManager.getMovieIdByScreeningId(request.getScreeningId());
        buyerManager.addWatchedMovie(buyer.getBuyerId(), movieId);
    }

    private long toStripeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new CinePachoException("El precio del producto no puede ser nulo");
        }
        return amount.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
