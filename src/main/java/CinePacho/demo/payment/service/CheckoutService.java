package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.request.SnackSelectionRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.auxiliaryClass.SnackManager;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.shared.enumeration.SeatType;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import CinePacho.demo.snacks.entities.SnackEntity;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final SeatManager seatManager;
    private final CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager;
    private final SnackManager snackManager;
    private final JwtService jwtService;
    private final UserManager userManager;

    @Autowired
    public CheckoutService(SeatManager seatManager, CinePacho.demo.shared.auxiliaryClass.SeatScreeningManager seatScreeningManager, SnackManager snackManager, JwtService jwtService, UserManager userManager ) {
        this.seatManager = seatManager;
        this.seatScreeningManager = seatScreeningManager;
        this.snackManager = snackManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
    }



    /**
     * Genera una vista previa del resumen de la compra sin modificar los inventarios ni el estado de las sillas.
     * @param request La solicitud con las sillas, snacks y la función elegidas.
     * @param token El token del usuario para validar permisos y extraer correo si es necesario.
     * @return CheckoutSummaryResponse con los totales calculados.
     */
    public CheckoutSummaryResponse preview(CheckoutRequest request, String token) {
        // Vista previa: calcula y valida sin modificar inventarios ni sillas
        return buildSummary(request, token);
    }

    /**
     * Genera el resumen final y confirma la disponibilidad de sillas y snacks antes de proceder al pago real.
     * @param request La solicitud de compra.
     * @param token El token del usuario.
     * @return CheckoutSummaryResponse con los totales calculados y estado confirmado para Stripe.
     */
    public CheckoutSummaryResponse confirm(CheckoutRequest request, String token) {
        // Confirmación: revalida disponibilidad antes de continuar al pago
        return buildSummary(request, token);
    }

    /**
     * Méeodo interno que construye el resumen calculando los precios de sillas y snacks, validando su existencia y disponibilidad.
     */
    private CheckoutSummaryResponse buildSummary(CheckoutRequest request, String token) {
        String userEmail = jwtService.extractEmail(token);

        List<UUID> seatIds = request.getSeats().stream()
                .map(SeatSelectionRequest::getSeatId)
                .collect(Collectors.toList());

        validateDuplicates("sillas", seatIds);

        List<SeatEntity> seats = seatManager.findAllByIdWithRoomAndMultiplex(seatIds);
        validateExistenceComplete("sillas", seatIds, seats.stream().map(SeatEntity::getId).collect(Collectors.toList()));

        validateSameRoomAndSameLocation(seats);
        validateSeatStatus(seats, userEmail, request.getScreeningId());

        BigDecimal generalPrice = seats.get(0).getRoom().getMultiplex().getGeneralSeatPrice();
        BigDecimal preferentialPrice = seats.get(0).getRoom().getMultiplex().getPreferentialSeatPrice();
        if (generalPrice == null || preferentialPrice == null) {
            throw new CinePachoException("El multiplex no tiene precios de silla configurados");
        }

        List<SeatSummaryResponse> seatSummaries = new ArrayList<>();
        BigDecimal totalSeats = BigDecimal.ZERO;
        for (SeatEntity seat : seats) {
            BigDecimal seatPrice = seat.getType() == SeatType.GENERAL ? generalPrice : preferentialPrice;
            totalSeats = totalSeats.add(seatPrice);
            seatSummaries.add(SeatSummaryResponse.builder()
                    .seatId(seat.getId())
                    .seatType(seat.getType().name())
                    .seatStatus(seat.getStatus())
                    .seatPrice(seatPrice)
                    .build());
        }

        List<SnackSelectionRequest> snackRequests = request.getSnacks() == null
                ? Collections.emptyList()
                : request.getSnacks();

        List<SnackSummaryResponse> snackSummaries = new ArrayList<>();
        BigDecimal totalSnacks = BigDecimal.ZERO;

        if (!snackRequests.isEmpty()) {
            List<UUID> snackIds = snackRequests.stream()
                    .map(SnackSelectionRequest::getSnackId)
                    .collect(Collectors.toList());

            validateDuplicates("snacks", snackIds);

            List<SnackEntity> snacks = snackManager.findAllById(snackIds);
            validateExistenceComplete("snacks", snackIds, snacks.stream().map(SnackEntity::getId).collect(Collectors.toList()));

            Map<UUID, SnackEntity> snackMap = snacks.stream()
                    .collect(Collectors.toMap(SnackEntity::getId, s -> s));

            for (SnackSelectionRequest snackRequest : snackRequests) {
                SnackEntity snack = snackMap.get(snackRequest.getSnackId());
                if (snack.getQuantity() < snackRequest.getQuantity()) {
                    throw new CinePachoException(
                            "Stock insuficiente para el snack '" + snack.getName() + "'");
                }

                BigDecimal unitPrice = snack.getPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(snackRequest.getQuantity()));
                totalSnacks = totalSnacks.add(subtotal);

                snackSummaries.add(SnackSummaryResponse.builder()
                        .snackId(snack.getId())
                        .nameSnack(snack.getName())
                        .quantity(snackRequest.getQuantity())
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build());
            }
        }

        return CheckoutSummaryResponse.builder()
                .multiplexId(seats.get(0).getRoom().getMultiplex().getId())
                .totalSeats(totalSeats)
                .totalSnacks(totalSnacks)
                .totalPurchase(totalSeats.add(totalSnacks))
                .seats(seatSummaries)
                .snacks(snackSummaries)
                .build();
    }

    private void validateDuplicates(String tipo, List<UUID> ids) {
        // Evita que el cliente duplique elementos en el cálculo
        Set<UUID> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new CinePachoException("Hay " + tipo + " duplicados en la solicitud");
        }
    }

    private void validateExistenceComplete(String tipo, List<UUID> idsSolicitados, List<UUID> idsEncontrados) {
        // Asegura que todos los ids enviados existan
        Set<UUID> faltantes = new HashSet<>(idsSolicitados);
        faltantes.removeAll(idsEncontrados);
        if (!faltantes.isEmpty()) {
            throw new CinePachoException("No se encontraron " + tipo + " con id: " + faltantes);
        }
    }

    private void validateSameRoomAndSameLocation(List<SeatEntity> seats) {
        UUID roomId = seats.get(0).getRoom().getId();
        UUID multiplexId = seats.get(0).getRoom().getMultiplex().getId();
        for (SeatEntity seat : seats) {
            if (!seat.getRoom().getId().equals(roomId)) {
                throw new CinePachoException("Todas las sillas deben pertenecer a la misma sala");
            }
            if (!seat.getRoom().getMultiplex().getId().equals(multiplexId)) {
                throw new CinePachoException("Todas las sillas deben pertenecer al mismo multiplex");
            }
        }
    }

    private void validateSeatStatus(List<SeatEntity> seats, String userEmail, java.util.UUID screeningId) {
        // Valida disponibilidad y bloqueo por usuario antes de continuar — ahora por función
        for (SeatEntity seat : seats) {
            var ss = seatScreeningManager.getSeatScreening(seat.getId(), screeningId);
            if (ss != null) {
                if (ss.getStatus() == SeatStatus.SOLD) {
                    throw new CinePachoException("La silla " + seat.getId() + " ya fue vendida para esta función");
                }
                if (ss.getStatus() == SeatStatus.BLOCKED) {
                    if (ss.getBlockedByUserEmail() == null || !ss.getBlockedByUserEmail().equals(userEmail)) {
                        throw new CinePachoException("La silla " + seat.getId() + " está bloqueada por otro usuario para esta función");
                    }
                }
            }
        }
    }



    public UUID getUserIdFromToken(String token) {
        String email = jwtService.extractEmail(token);
        
        System.out.println("Email extraído del token: " + email); // Debug: Verificar email extraído

        UserEntity userEntity = userManager.getUserByEmail(email);

        System.out.println("Usuario encontrado: " + userEntity.getUsername() + " con ID: " + userEntity.getUserId()); // Debug: Verificar usuario encontrado
        UUID userId = userEntity.getUserId();

        return userId;
    }
}
