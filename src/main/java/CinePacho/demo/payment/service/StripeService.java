package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.request.SnackSelectionRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.reports.entities.SnackSaleEntity;
import CinePacho.demo.reports.entities.TicketSaleEntity;
import CinePacho.demo.reports.repository.SnackSaleRepository;
import CinePacho.demo.reports.repository.TicketSaleRepository;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.auxiliaryClass.SnackManager;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.shared.enumeration.SeatType;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import CinePacho.demo.snacks.entities.SnackEntity;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StripeService {

    private final CheckoutService checkoutService;
    private final PaymentRepository paymentRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final SnackSaleRepository snackSaleRepository;
    private final MultiplexRepository multiplexRepository;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SeatManager seatManager;
    private final SnackManager snackManager;

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
            SnackSaleRepository snackSaleRepository,
            MultiplexRepository multiplexRepository,
            BuyerManager buyerManager,
            MovieManager movieManager,
            JwtService jwtService,
            UserManager userManager,
            SeatManager seatManager,
            SnackManager snackManager
    ) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.ticketSaleRepository = ticketSaleRepository;
        this.snackSaleRepository = snackSaleRepository;
        this.multiplexRepository = multiplexRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
        this.seatManager = seatManager;
        this.snackManager = snackManager;
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
        // Se incluye el paymentId en la respuesta para que el frontend pueda usarlo en el webhook de confirmación.
        // Esto es necesario para testing manual en Postman o para llamadas directas al webhook.
        summary.setPaymentId(payment.getPaymentId());

        return summary;
    }

    /**
     * Método invocado cuando Stripe confirma el pago exitoso (via webhook).
     * Realiza todas las acciones que dependen de un pago confirmado:
     * 1. Valida que el usuario actual sea el propietario de todas las sillas bloqueadas
     * 2. Actualiza el estado del pago a COMPLETED
     * 3. Cambia el estado de las sillas de BLOCKED a SOLD
     * 4. Registra la venta de tickets para el módulo de reportes
     * 5. Registra la venta de snacks para el módulo de reportes
     * 6. Agrega la película al historial del comprador
     * 7. Programa la liberación automática de sillas en 3 horas
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
        // Se obtiene la screening para acceder al multiplex y película.
        MovieScreening screening = movieManager.getMovieScreeningById(checkoutRequest.getScreeningId());
        BigDecimal totalSeatsAmount = calculateSeatsTotal(seats);
        TicketSaleEntity ticketSale = TicketSaleEntity.builder()
                .multiplex(screening.getRoom().getMultiplex())
                .screening(screening)
                .soldAt(LocalDateTime.now())
                .ticketsQuantity(seatIds.size())
                // Se registra solo el total de sillas para no mezclar con snacks.
                .totalAmount(totalSeatsAmount)
                .build();
        ticketSaleRepository.save(ticketSale);

        // PASO 5: Registrar la venta de snacks para el módulo de reportes mensuales.
        registerSnackSales(checkoutRequest);

        // PASO 6: Agregar la película al historial de películas vistas por el comprador.
        UserEntity currentUser = userManager.getUserByEmail(userEmail);
        if (currentUser.getUserType() == UserType.BUYER) {
            BuyerEntity buyer = buyerManager.getBuyerByEmail(userEmail);
            Long movieId = movieManager.getMovieIdByScreeningId(checkoutRequest.getScreeningId());
            buyerManager.addWatchedMovie(buyer.getBuyerId(), movieId);
        }

        // PASO 7: Programar la liberación automática de sillas 3 horas después del inicio de la función.
        // Esto garantiza que si el comprador no asiste, las sillas se liberen automáticamente.
        seatManager.scheduleRelease(
                screening.getId(),
                screening.getRoom().getId(),
                screening.getDateTime()
        );
    }

    private BigDecimal calculateSeatsTotal(List<SeatEntity> seats) {
        // Calcula el total de sillas usando los precios configurados en el multiplex.
        if (seats.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal generalPrice = seats.get(0).getRoom().getMultiplex().getGeneralSeatPrice();
        BigDecimal preferentialPrice = seats.get(0).getRoom().getMultiplex().getPreferentialSeatPrice();
        if (generalPrice == null || preferentialPrice == null) {
            throw new CinePachoException("El multiplex no tiene precios de silla configurados");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SeatEntity seat : seats) {
            BigDecimal seatPrice = seat.getType() == SeatType.GENERAL ? generalPrice : preferentialPrice;
            total = total.add(seatPrice);
        }
        return total;
    }

    private void registerSnackSales(CheckoutRequest checkoutRequest) {
        // Registra ventas de snacks (si existen) agrupadas por snack y multiplex.
        if (checkoutRequest.getSnacks() == null || checkoutRequest.getSnacks().isEmpty()) {
            return;
        }

        List<UUID> snackIds = checkoutRequest.getSnacks().stream()
                .map(snackSelection -> snackSelection.getSnackId())
                .collect(Collectors.toList());

        List<SnackEntity> snacks = snackManager.findAllById(snackIds);
        if (snacks.size() != snackIds.size()) {
            throw new CinePachoException("No se encontraron snacks para registrar la venta");
        }

        Map<UUID, SnackEntity> snackMap = snacks.stream()
                .collect(Collectors.toMap(SnackEntity::getId, snack -> snack));

        for (SnackSelectionRequest snackSelection : checkoutRequest.getSnacks()) {
            SnackEntity snack = snackMap.get(snackSelection.getSnackId());
            BigDecimal unitPrice = snack.getPrice();
            if (unitPrice == null) {
                throw new CinePachoException("El snack " + snack.getId() + " no tiene precio configurado");
            }
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(snackSelection.getQuantity()));

            SnackSaleEntity snackSale = SnackSaleEntity.builder()
                    .multiplex(snack.getMultiplex())
                    .snack(snack)
                    .soldAt(LocalDateTime.now())
                    .snacksQuantity(snackSelection.getQuantity())
                    .totalAmount(total)
                    .build();
            snackSaleRepository.save(snackSale);
        }
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
