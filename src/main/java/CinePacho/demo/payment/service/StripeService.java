package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.reports.entities.TicketSaleEntity;
import CinePacho.demo.reports.repository.TicketSaleRepository;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StripeService {

    private final CheckoutService checkoutService;
    private final PaymentRepository paymentRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final MultiplexRepository multiplexRepository;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SeatManager seatManager;
    private final CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CURRENCY = "COP";

    @Autowired
    public StripeService(
            CheckoutService checkoutService,
            PaymentRepository paymentRepository,
            TicketSaleRepository ticketSaleRepository,
            MultiplexRepository multiplexRepository,
            BuyerManager buyerManager,
            MovieManager movieManager,
            JwtService jwtService,
            UserManager userManager, SeatManager seatManager, CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager
    ) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.ticketSaleRepository = ticketSaleRepository;
        this.multiplexRepository = multiplexRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
        this.seatManager = seatManager;
        this.seatScreeningManager = seatScreeningManager;
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

        // CORRECCIÓN: Se crea el pago con estado PENDING.
        // IMPORTANTE: El cambio a COMPLETED debería ocurrir en el webhook de Stripe después de confirmación exitosa.
        // Por ahora se mantiene PENDING aquí y se actualiza cuando la sesión es confirmada.
        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(checkoutService.getUserIdFromToken(token));
        payment.setAmount(summary.getTotalPurchase());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Se retorna la sesión de Stripe sin procesar cambios de estado ni registrar ventas.
        // Los cambios de estado y registro de ventas ocurrirán SOLO después de que Stripe confirme el pago.
        summary.setStatus("SUCCESS");
        summary.setMessage("Checkout creado correctamente. Por favor completar el pago en Stripe.");
        summary.setSessionId(session.getId());
        summary.setSessionUrl(session.getUrl());

        //añadir película al historuiial de pelis vistas por el usuario
        registerWatchedMovieForBuyer(token, request, summary, payment);

        //cambiar el estado de las sillas a vendidas solo cuando estén BLOCKED para ESTA FUNCIÓN
        request.getSeats().forEach(
                        seatsIDs -> {
                            var ss = seatScreeningManager.getSeatScreening(seatsIDs.getSeatId(), request.getScreeningId());
                            if (ss == null || ss.getStatus() != SeatStatus.BLOCKED) {
                                throw new CinePachoException("La silla no ha sido seleccionada previamente para esta función");
                            }
                            // si están BLOCKED, cambiar el estado a SOLD para esta función
                            seatScreeningManager.markSold(seatsIDs.getSeatId(), request.getScreeningId());
                            System.out.printf("Silla %s cambiada a SOLD para la función %s", seatsIDs.getSeatId(), request.getScreeningId());
                        });
    /**
     * Método invocado cuando Stripe confirma el pago exitoso (via webhook).
     * Realiza todas las acciones que dependen de un pago confirmado:
     * 1. Valida que el usuario actual sea el propietario de todas las sillas bloqueadas
     * 2. Actualiza el estado del pago a COMPLETED
     * 3. Cambia el estado de las sillas de BLOCKED a SOLD
     * 4. Registra la venta de tickets para el módulo de reportes
     * 5. Agrega la película al historial del comprador
     * 6. Programa la liberación automática de sillas en 3 horas
     *
     * IMPORTANTE: Este método debe ser llamado SOLAMENTE después de que Stripe confirme el pago.
     * Idealmente desde un webhook: POST /api/checkout/stripe/webhook
     *
     * @param checkoutRequest Información de la compra original
     * @param paymentId ID del pago que fue confirmado
     * @param userEmail Email del usuario que realizó la compra (del JWT)
     * @throws CinePachoException Si la validación falla (usuario no coincide, sillas no disponibles, etc)
     */
    public void handlePaymentSuccess(CheckoutRequest checkoutRequest, UUID paymentId, String userEmail) {
        // PASO 1: Validar que el usuario actual sea quien bloqueó las sillas.
        // Esto previene que múltiples usuarios compitan por la misma silla bloqueada.
        List<UUID> seatIds = checkoutRequest.getSeats().stream()
                .map(SeatSelectionRequest::getSeatId)
                .collect(Collectors.toList());

        List<SeatEntity> seats = seatManager.findAllByIdWithRoomAndMultiplex(seatIds);

        // Verificar que TODAS las sillas pertenecen al usuario actual y están en estado BLOCKED.
        for (SeatEntity seat : seats) {
            if (seat.getStatus() != SeatStatus.BLOCKED) {
                throw new CinePachoException("La silla " + seat.getId() + " no está bloqueada. Estado actual: " + seat.getStatus());
            }
            if (seat.getBlockedByUserEmail() == null || !seat.getBlockedByUserEmail().equals(userEmail)) {
                // PREVENCIÓN DE RACE CONDITION: Si otro usuario bloqueó esta silla después de este usuario,
                // rechazar el pago para garantizar consistencia. El usuario debe hacer checkout nuevamente.
                throw new CinePachoException("La silla " + seat.getId() + " fue bloqueada por otro usuario. Por favor intente nuevamente.");
            }
        }

        // PASO 2: Actualizar el estado del pago a COMPLETED.
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CinePachoException("Pago no encontrado con ID: " + paymentId));
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // PASO 3: Cambiar el estado de las sillas de BLOCKED a SOLD.
        checkoutRequest.getSeats().forEach(seatSelection -> {
            seatManager.updateSeatStatus(seatSelection.getSeatId(), SeatStatus.SOLD);
        });

        // PASO 4: Registrar la venta de tickets para el módulo de reportes mensales.
        // Solo se registran tickets (sillas), no snacks, para que los reportes muestren ingresos por películas.
        // Se obtiene la screening para acceder al multiplex y película.
        MovieScreening screening = movieManager.getMovieScreeningById(checkoutRequest.getScreeningId());
        TicketSaleEntity ticketSale = TicketSaleEntity.builder()
                .multiplex(screening.getRoom().getMultiplex())
                .screening(screening)
                .soldAt(LocalDateTime.now())
                .ticketsQuantity(seatIds.size())
                .totalAmount(payment.getAmount())
                .build();
        ticketSaleRepository.save(ticketSale);

        // PASO 5: Agregar la película al historial de películas vistas por el comprador.
        UserEntity currentUser = userManager.getUserByEmail(userEmail);
        if (currentUser.getUserType() == UserType.BUYER) {
            BuyerEntity buyer = buyerManager.getBuyerByEmail(userEmail);
            Long movieId = movieManager.getMovieIdByScreeningId(checkoutRequest.getScreeningId());
            buyerManager.addWatchedMovie(buyer.getBuyerId(), movieId);
        }

        // PASO 6: Programar la liberación automática de sillas 3 horas después del inicio de la función.
        // Esto garantiza que si el comprador no asiste, las sillas se liberen automáticamente.
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
