package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.response.BillingDTO;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.BillingEntity;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.BillingRepository;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.reports.entities.TicketSaleEntity;
import CinePacho.demo.reports.repository.TicketSaleRepository;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StripeService {

    private final CheckoutService checkoutService;
    private final PaymentRepository paymentRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final EmailService emailService;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SeatManager seatManager;
    private final SeatScreeningManager seatScreeningManager;
    private final BillingService billingService;
    private final BillingRepository billingRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CURRENCY = "COP";

    @Autowired
    public StripeService(
            CheckoutService checkoutService,
            PaymentRepository paymentRepository,
            TicketSaleRepository ticketSaleRepository, EmailService emailService,
            BuyerManager buyerManager,
            MovieManager movieManager,
            JwtService jwtService,
            UserManager userManager, SeatManager seatManager, SeatScreeningManager seatScreeningManager, BillingService billingService,
            BillingRepository billingRepository) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.ticketSaleRepository = ticketSaleRepository;
        this.emailService = emailService;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
        this.seatManager = seatManager;
        this.seatScreeningManager = seatScreeningManager;
        this.billingService = billingService;
        this.billingRepository = billingRepository;
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
        summary.setStatus("PENDING");
        summary.setMessage("Checkout creado correctamente. Complete el pago en Stripe.");
        summary.setSessionId(session.getId());
        summary.setSessionUrl(session.getUrl());
        summary.setPaymentId(payment.getPaymentId());

        //Creación de factura con QR (cuando se aplique que el tokens ea de un employee, reemplazar esto por simplemente buscar por email, pues se requerirá el email del buyer esi es un employee quién está atendiendo)
        BillingEntity billing = billingService.createBilling(payment,buyerManager.getBuyerByEmail(jwtService.extractEmail(token)), summary, movieManager.getMovieScreeningById(request.getScreeningId()));


        //agrega el id de la fatura para que el front lo reciba
        summary.setBillingId(billing.getId());

        // NO registrar películas ni cambiar estados aquí: la confirmación y cambios se realizan cuando Stripe notifica el pago (handlePaymentSuccess)
        return summary;
    }

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
         * @param token token del usuario que realizó la compra (del JWT)
         * @throws CinePachoException Si la validación falla (usuario no coincide, sillas no disponibles, etc)
         */



        @Transactional
        public Map<String, String> handlePaymentSuccess(CheckoutRequest checkoutRequest, UUID paymentId, String token){

            String userEmail = jwtService.extractEmail(token);

            // Validación por función: asegurar que las reservas (SeatScreening) para esta función están bloqueadas por este usuario
            List<UUID> seatIds = checkoutRequest.getSeats().stream()
                    .map(SeatSelectionRequest::getSeatId)
                    .collect(Collectors.toList());

            for (UUID seatId : seatIds) {
                var ss = seatScreeningManager.getSeatScreening(seatId, checkoutRequest.getScreeningId());
                if (ss == null) {
                    throw new CinePachoException("La silla " + seatId + " no está reservada para esta función");
                }
                if (ss.getStatus() != SeatStatus.BLOCKED) {
                    throw new CinePachoException("La silla " + seatId + " no está bloqueada para esta función. Estado: " + ss.getStatus());
                }
                if (ss.getBlockedByUserEmail() == null || !ss.getBlockedByUserEmail().equals(userEmail)) {
                    throw new CinePachoException("La silla " + seatId + " fue bloqueada por otro usuario. Por favor intente nuevamente.");
                }
            }

            // Actualizar el estado del pago a COMPLETED
            PaymentEntity payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new CinePachoException("Pago no encontrado con ID: " + paymentId));
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // Marcar cada SeatScreening como SOLD
            checkoutRequest.getSeats().forEach(seatSelection -> {
                seatScreeningManager.markSold(seatSelection.getSeatId(), checkoutRequest.getScreeningId());
            });

            // Registrar la venta en el módulo de reportes
            MovieScreening screening = movieManager.getMovieScreeningById(checkoutRequest.getScreeningId());
            TicketSaleEntity ticketSale = TicketSaleEntity.builder()
                    .multiplex(screening.getRoom().getMultiplex())
                    .screening(screening)
                    .soldAt(LocalDateTime.now())
                    .ticketsQuantity(seatIds.size())
                    .totalAmount(payment.getAmount())
                    .build();
            ticketSaleRepository.save(ticketSale);

            //recupero la factura de la BD
            BillingEntity billing = billingRepository.findByPayment_PaymentId(paymentId);
            //Creo DTO con esa factura
            BillingDTO bdt = BillingDTO.builder()
                        .status("COMPLETED")
                        .message("Pago exitoso")
                        .totalSeats(billing.getTotalSeats())
                        .totalSnacks(billing.getTotalSnacks())
                        .totalPurchase(billing.getTotalPurchase())
                        .seats(null)
                        .snacks(null)
                        .roomNumber(billing.getRoomNumber())
                        .seatsNumbers(null)
                        .movieTitle(billing.getMovieTitle())
                        .screeningDate(billing.getScreeningDate())
                        .build();
            //Enviar a correo el QR generado en check

            emailService.sendBillingEmail(userEmail,userManager.getUserByEmail(userEmail).getUsername(),bdt,billing.getQrBase64());

            // Agregar película al historial del comprador usando el método existente registerWatchedMovieForBuyer
            try {
                registerWatchedMovieForBuyerByEmail(userEmail, checkoutRequest);
            } catch (Exception e){
                // No detener el flujo principal si hay un problema reportando el historial; loguear y continuar
                System.out.printf("Advertencia: no fue posible registrar la película en el historial: %s", e.getMessage());
            }

            // Programar la liberación automática de sillas 3 horas después del inicio de la función
            seatManager.scheduleRelease(
                    screening.getId(),
                    screening.getRoom().getId(),
                    screening.getDateTime()
            );
            return Map.of("message:", "Pago realizado con éxito");
        }


        private void registerWatchedMovieForBuyer (
                String token,
                CheckoutRequest request,
                CheckoutSummaryResponse summary,
                PaymentEntity payment
    ){
            String userEmail = jwtService.extractEmail(token);
            UserEntity currentUser = userManager.getUserByEmail(userEmail);

            if (currentUser.getUserType() != UserType.BUYER || !summary.getStatus().equals("SUCCESS") || payment.getUserId() == null) {
                throw new CinePachoException("El usuario no es un comprador o el pago no fue exitoso");

            }

            BuyerEntity buyer = buyerManager.getBuyerByEmail(userEmail);
            Long movieId = movieManager.getMovieIdByScreeningId(request.getScreeningId());
            buyerManager.addWatchedMovie(buyer.getBuyerId(), movieId);
        }

        // Versión alternativa que acepta email en vez de token: útil para webhooks que no llevan JWT
        private void registerWatchedMovieForBuyerByEmail(String userEmail, CheckoutRequest request){
            UserEntity currentUser = userManager.getUserByEmail(userEmail);
            if (currentUser.getUserType() != UserType.BUYER) {
                throw new CinePachoException("El usuario no es un comprador");
            }
            BuyerEntity buyer = buyerManager.getBuyerByEmail(userEmail);
            Long movieId = movieManager.getMovieIdByScreeningId(request.getScreeningId());
            buyerManager.addWatchedMovie(buyer.getBuyerId(), movieId);
        }

        private long toStripeAmount (BigDecimal amount){
            if (amount == null) {
                throw new CinePachoException("El precio del producto no puede ser nulo");
            }
            return amount.setScale(0, RoundingMode.HALF_UP).longValue();
        }

}