package CinePacho.demo.payment.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.request.SnackSelectionRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.dto.response.SeatSummaryResponse;
import CinePacho.demo.payment.dto.response.SnackSummaryResponse;
import CinePacho.demo.seats.entities.SeatEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import CinePacho.demo.shared.auxiliaryClass.SnackManager;
import CinePacho.demo.shared.enumeration.SeatType;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import CinePacho.demo.snacks.entities.SnackEntity;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final SeatManager seatManager;
    private final SnackManager snackManager;
    private final JwtService jwtService;

    @Value("${mercadopago.access.token}")
    public String accessTokenPayment;

    @Autowired
    public CheckoutService(SeatManager seatManager, SnackManager snackManager, JwtService jwtService, @Value("${mercadopago.access.token}") String accessTokenPayment) {
        this.seatManager = seatManager;
        this.snackManager = snackManager;
        this.jwtService = jwtService;
        this.accessTokenPayment = accessTokenPayment;
    }



    public CheckoutSummaryResponse preview(CheckoutRequest request, String token) {
        // Vista previa: calcula y valida sin modificar inventarios ni sillas
        return buildSummary(request, token);
    }

    public CheckoutSummaryResponse confirm(CheckoutRequest request, String token) {
        // Confirmación: revalida disponibilidad antes de continuar al pago
        return buildSummary(request, token);
    }

    private CheckoutSummaryResponse buildSummary(CheckoutRequest request, String token) {
        String userEmail = jwtService.extractEmail(token);

        List<UUID> seatIds = request.getSeats().stream()
                .map(SeatSelectionRequest::getSeatId)
                .collect(Collectors.toList());

        validarDuplicados("sillas", seatIds);

        List<SeatEntity> seats = seatManager.findAllByIdWithRoomAndMultiplex(seatIds);
        validarExistenciaCompleta("sillas", seatIds, seats.stream().map(SeatEntity::getId).collect(Collectors.toList()));

        validarMismaSalaYMismaSede(seats);
        validarEstadoDeSillas(seats, userEmail);

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

            validarDuplicados("snacks", snackIds);

            List<SnackEntity> snacks = snackManager.findAllById(snackIds);
            validarExistenciaCompleta("snacks", snackIds, snacks.stream().map(SnackEntity::getId).collect(Collectors.toList()));

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

    private void validarDuplicados(String tipo, List<UUID> ids) {
        // Evita que el cliente duplique elementos en el cálculo
        Set<UUID> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new CinePachoException("Hay " + tipo + " duplicados en la solicitud");
        }
    }

    private void validarExistenciaCompleta(String tipo, List<UUID> idsSolicitados, List<UUID> idsEncontrados) {
        // Asegura que todos los ids enviados existan
        Set<UUID> faltantes = new HashSet<>(idsSolicitados);
        faltantes.removeAll(idsEncontrados);
        if (!faltantes.isEmpty()) {
            throw new CinePachoException("No se encontraron " + tipo + " con id: " + faltantes);
        }
    }

    private void validarMismaSalaYMismaSede(List<SeatEntity> seats) {
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

    private void validarEstadoDeSillas(List<SeatEntity> seats, String userEmail) {
        // Valida disponibilidad y bloqueo por usuario antes de continuar
        for (SeatEntity seat : seats) {
            if (seat.getStatus() == SeatStatus.SOLD) {
                throw new CinePachoException("La silla " + seat.getId() + " ya fue vendida");
            }
            if (seat.getStatus() == SeatStatus.BLOCKED) {
                if (seat.getBlockedByUserEmail() == null || !seat.getBlockedByUserEmail().equals(userEmail)) {
                    throw new CinePachoException("La silla " + seat.getId() + " está bloqueada por otro usuario");
                }
            }
        }
    }
}
