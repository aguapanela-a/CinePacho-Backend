package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.request.SnackSelectionRequest;
import CinePacho.demo.payment.dto.response.BillingDTO;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.payment.entities.BillingEntity;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.PaymentStatus;
import CinePacho.demo.payment.repository.BillingRepository;
import CinePacho.demo.payment.repository.PaymentRepository;
import CinePacho.demo.reports.entities.SnackSaleEntity;
import CinePacho.demo.reports.entities.TicketSaleEntity;
import CinePacho.demo.reports.repository.SnackSaleRepository;
import CinePacho.demo.reports.repository.TicketSaleRepository;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.*;
import CinePacho.demo.multiplex.repository.MultiplexRepository;
import CinePacho.demo.shared.enumeration.SeatType;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import CinePacho.demo.snacks.entities.SnackEntity;
import CinePacho.demo.snacks.repository.SnackRepository;
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
import java.time.ZoneId;
import java.util.*;
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
    private final EmailService emailService;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;
    private final JwtService jwtService;
    private final UserManager userManager;
    private final SeatManager seatManager;
    private final SeatScreeningManager seatScreeningManager;
    private final BillingService billingService;
    private final BillingRepository billingRepository;
    private final CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider employeeMultiplexProvider;
    private final SnackRepository snackRepository;
    private final CinePacho.demo.shared.auxiliaryClass.PointsManager pointsManager;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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
            BillingRepository billingRepository, CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider employeeMultiplexProvider,
            SnackRepository snackRepository, CinePacho.demo.shared.auxiliaryClass.PointsManager pointsManager) {
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.ticketSaleRepository = ticketSaleRepository;
        this.snackSaleRepository = null;
        this.emailService = emailService;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
        this.seatManager = seatManager;
        this.seatScreeningManager = seatScreeningManager;
        this.billingService = billingService;
        this.billingRepository = billingRepository;
        this.employeeMultiplexProvider = employeeMultiplexProvider;
        this.snackRepository = snackRepository;
        this.pointsManager = pointsManager;
    }

    public CheckoutSummaryResponse checkoutProducts(CheckoutRequest request, String token) throws StripeException {


        System.out.println("@@@@@@@@@@@@@@ multiplexid del snack: " + request.getSnacks().get(0).getMultiplexId() + "");

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

        String buyerEmail;
        String actorEmail = jwtService.extractEmail(token);
        UserEntity actorUser = userManager.getUserByEmail(actorEmail);
        MovieScreening screening = movieManager.getMovieScreeningById(request.getScreeningId());


        
        if (actorUser.getUserType() == UserType.BUYER) {
                buyerEmail = actorEmail;
            } else {
                // actor es empleado/manager -> checkoutRequest debe incluir buyerEmail
                if (request.getBuyerEmail() == null) {
                    throw new CinePachoException("Cuando un empleado confirma el pago, debe incluir el email del comprador en la solicitud");
                }
                buyerEmail = request.getBuyerEmail();
                // validar que el empleado pertenece al multiplex de la función
                java.util.UUID empMultiplex = employeeMultiplexProvider.getMultiplexIdByUserEmail(actorEmail);
                if (!empMultiplex.equals(screening.getRoom().getMultiplex().getId())) {
                    throw new CinePachoException("El empleado no pertenece a este multiplex");
                }
            
        }

        // Validación por función: asegurar que las reservas (SeatScreening) para esta función están bloqueadas por el buyer
        List<UUID> seatIds = request.getSeats().stream()
                .map(SeatSelectionRequest::getSeatId)
                .collect(Collectors.toList());
        for (UUID seatId : seatIds) {
            var ss = seatScreeningManager.getSeatScreening(seatId, request.getScreeningId());
            if (ss == null) {
                throw new CinePachoException("La silla " + seatId + " no está reservada para esta función");
            }
            if (ss.getStatus() != SeatStatus.BLOCKED) {
                throw new CinePachoException("La silla " + seatId + " no está bloqueada para esta función. Estado: " + ss.getStatus());
            }
            if (ss.getBlockedByUserEmail() == null || !ss.getBlockedByUserEmail().equals(buyerEmail)) {
                throw new CinePachoException("La silla " + seatId + " fue bloqueada por otro usuario. Por favor intente nuevamente.");
            }
        }

        // El pago se asocia al buyer correcto según el rol del actor
        String buyerEmailForPayment;
        if (actorUser.getUserType() != UserType.BUYER) {
            // actor es empleado/manager → buyerEmail ya fue validado arriba
            buyerEmailForPayment = request.getBuyerEmail();
        } else {
            buyerEmailForPayment = actorEmail;
        }

        // Crear y persistir el PaymentEntity ANTES de crear la sesión Stripe,
        // para poder incluir el paymentId en la successUrl de retorno.
        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(buyerManager.getBuyerByEmail(buyerEmailForPayment).getBuyerId());
        payment.setAmount(summary.getTotalPurchase());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Las URLs apuntan al FRONTEND (React) con el paymentId en la query string
        String successUrl = frontendUrl + "/stripe/success?paymentId=" + payment.getPaymentId();
        String cancelUrl  = frontendUrl + "/stripe/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);

        // Poblar la respuesta con los datos de la sesión y el pago
        summary.setStatus("PENDING");
        summary.setMessage("Checkout creado correctamente. Complete el pago en Stripe.");
        summary.setSessionId(session.getId());
        summary.setSessionUrl(session.getUrl());
        summary.setPaymentId(payment.getPaymentId());

        //Creación de factura con QR: usar buyerEmailForPayment
        BillingEntity billing = billingService.createBilling(payment, buyerManager.getBuyerByEmail(buyerEmailForPayment), summary, screening);

        //agrega el id de la fatura para que el front lo reciba
        summary.setBillingId(billing.getId());

        // NO registrar películas ni cambiar estados aquí: la confirmación y cambios se realizan cuando el frontend confirma (handlePaymentSuccess)
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

            // Determinar actor y buyerEmail
            String actorEmail = jwtService.extractEmail(token);
            UserEntity actorUser = userManager.getUserByEmail(actorEmail);
            String buyerEmail;
            MovieScreening screening = movieManager.getMovieScreeningById(checkoutRequest.getScreeningId());

            


            if (actorUser.getUserType() == UserType.BUYER) {
                buyerEmail = actorEmail;
            } else {
                // actor es empleado/manager -> checkoutRequest debe incluir buyerEmail
                if (checkoutRequest.getBuyerEmail() == null) {
                    throw new CinePachoException("Cuando un empleado confirma el pago, debe incluir el email del comprador en la solicitud");
                }
                buyerEmail = checkoutRequest.getBuyerEmail();
                // validar que el empleado pertenece al multiplex de la función
                java.util.UUID empMultiplex = employeeMultiplexProvider.getMultiplexIdByUserEmail(actorEmail);
                if (!empMultiplex.equals(screening.getRoom().getMultiplex().getId())) {
                    throw new CinePachoException("El empleado no pertenece a este multiplex");
                }
            }

            //MOVER VALIDACION
            // Validación por función: asegurar que las reservas (SeatScreening) para esta función están bloqueadas por el buyer
            List<UUID> seatIds = checkoutRequest.getSeats().stream()
                    .map(SeatSelectionRequest::getSeatId)
                    .collect(Collectors.toList());

            

            // Actualizar el estado del pago a COMPLETED
            PaymentEntity payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new CinePachoException("Pago no encontrado con ID: " + paymentId));
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            

            // Restar snacks comprados del inventario por multiplex
            List<SnackSelectionRequest> snackSelections = checkoutRequest.getSnacks() == null
                    ? Collections.emptyList()
                    : checkoutRequest.getSnacks();

            // Multiplex de la función para validar inventario correcto
            UUID screeningMultiplexId = screening.getRoom().getMultiplex().getId();
            for (SnackSelectionRequest snackSelection : snackSelections) {
                if (snackSelection.getQuantity() == null || snackSelection.getQuantity() <= 0) {
                    continue;
                }

                // Asegurar que el cliente envíe el multiplexId y que coincida con la función
                if (snackSelection.getMultiplexId() == null) {
                    throw new CinePachoException("El multiplexId del snack es obligatorio");
                }
                if (!screeningMultiplexId.equals(snackSelection.getMultiplexId())) {
                    throw new CinePachoException("El snack no pertenece al multiplex de la función seleccionada");
                }

                    SnackEntity snack = snackRepository.findById(snackSelection.getSnackId())
                        .orElseThrow(() -> new CinePachoException("Snack no encontrado con ID: " + snackSelection.getSnackId()));

                java.util.UUID multiplexId = snackSelection.getMultiplexId();
                if (snack.getMultiplex() == null || !snack.getMultiplex().getId().equals(multiplexId)) {
                    throw new CinePachoException("El snack no pertenece al multiplex indicado");
                }

                Integer currentStock = snack.getQuantity();
                if (currentStock == null || currentStock < snackSelection.getQuantity()) {
                    throw new CinePachoException("Stock insuficiente para el snack: " + snack.getName());
                }

                snack.setQuantity(currentStock - snackSelection.getQuantity());
                snackRepository.save(snack);
            }

            // Marcar cada SeatScreening como SOLD
            checkoutRequest.getSeats().forEach(seatSelection -> {
                seatScreeningManager.markSold(seatSelection.getSeatId(), checkoutRequest.getScreeningId());
            });

                // Calcular y asignar puntos según configuración (delegar a PointsManager)
                try {
                    BuyerEntity buyer = buyerManager.getBuyerByEmail(buyerEmail);
                    pointsManager.processPurchase(buyer.getBuyerId(), checkoutRequest);
                } catch (Exception e) {
                    // No interrumpir el flujo principal si hay problema asignando puntos
                    System.out.printf("Advertencia: fallo al asignar puntos: %s", e.getMessage());
                }

                // Registrar la venta en el módulo de reportes
            TicketSaleEntity ticketSale = TicketSaleEntity.builder()
                    .multiplex(screening.getRoom().getMultiplex())
                    .screening(screening)
                    .soldAt(LocalDateTime.now(ZoneId.of("America/Bogota")))
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

            emailService.sendBillingEmail(buyerEmail,userManager.getUserByEmail(buyerEmail).getUsername(),bdt,billing.getQrBase64());

            // Agregar película al historial del comprador usando el método existente registerWatchedMovieForBuyer
            try {
                registerWatchedMovieForBuyerByEmail(buyerEmail, checkoutRequest);
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
            return Map.of("message", "Pago realizado con éxito");
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
