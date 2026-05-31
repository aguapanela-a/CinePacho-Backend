package CinePacho.demo.points.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.points.entities.PointsConfigEntity;
import CinePacho.demo.points.entities.PointsGainedEntity;
import CinePacho.demo.points.entities.VoucherEntity;
import CinePacho.demo.points.repository.PointsConfigRepository;
import CinePacho.demo.points.repository.PointsGainedRepository;
import CinePacho.demo.points.repository.VoucherRepository;
import CinePacho.demo.seats.entities.SeatScreeningEntity;
import CinePacho.demo.seats.repository.SeatScreeningRepository;
import CinePacho.demo.snacks.entities.SnackEntity;
import CinePacho.demo.snacks.repository.SnackRepository;
import CinePacho.demo.shared.auxiliaryClass.PointsManager;
import CinePacho.demo.shared.auxiliaryClass.PointsRecordDTO;
import CinePacho.demo.shared.auxiliaryClass.VoucherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsService implements PointsManager {

    private final PointsGainedRepository pointsGainedRepository;
    private final VoucherRepository voucherRepository;
    private final PointsConfigRepository pointsConfigRepository;
    private final BuyerRepository buyerRepository;
    private final SnackRepository snackRepository;
    private final SeatScreeningRepository seatScreeningRepository;

    @Override
    @Transactional
    public void processPurchase(UUID buyerId, Object checkoutRequest) {
        // Se espera que caller (StripeService) pase el CheckoutRequest para calcular puntos.
        if (checkoutRequest == null) return;
        try{
            // Reflection-lite: intentar acceder a getSnacks y getSeats métodos del checkoutRequest
            java.lang.reflect.Method getSnacks = checkoutRequest.getClass().getMethod("getSnacks");
            java.lang.reflect.Method getSeats = checkoutRequest.getClass().getMethod("getSeats");
            java.lang.reflect.Method getScreeningId = checkoutRequest.getClass().getMethod("getScreeningId");

            List<?> snacks = (List<?>) getSnacks.invoke(checkoutRequest);
            List<?> seats = (List<?>) getSeats.invoke(checkoutRequest);
            java.util.UUID screeningId = (java.util.UUID) getScreeningId.invoke(checkoutRequest);

            PointsConfigEntity cfg = pointsConfigRepository.findTopByOrderByIdDesc().orElse(null);
            boolean byUnit = cfg == null || cfg.isByUnit();

            int totalPoints = 0;

            if (snacks != null) {
                for (Object s : snacks) {
                    // cada s es SnackSelectionRequest con getSnackId() y getQuantity()
                    java.lang.reflect.Method getSnackId = s.getClass().getMethod("getSnackId");
                    java.lang.reflect.Method getQuantity = s.getClass().getMethod("getQuantity");
                    java.util.UUID snackId = (java.util.UUID) getSnackId.invoke(s);
                    Integer qty = (Integer) getQuantity.invoke(s);
                    SnackEntity snack = snackRepository.findById(snackId)
                            .orElse(null);
                    if (snack == null) continue;
                    Integer snackPoints = snack.getPoints() == null ? 0 : snack.getPoints();
                    if (byUnit) {
                        totalPoints += snackPoints * (qty == null ? 0 : qty);
                    } else {
                        if (qty != null && qty > 0) {
                            totalPoints += snackPoints; // solo una vez por tipo de snack
                        }
                    }
                }
            }

            if (seats != null && !seats.isEmpty()){
                if (byUnit){
                    for (Object seatSel : seats){
                        java.lang.reflect.Method getSeatId = seatSel.getClass().getMethod("getSeatId");
                        java.util.UUID seatId = (java.util.UUID) getSeatId.invoke(seatSel);
                        SeatScreeningEntity ss = seatScreeningRepository.findById(seatId).orElse(null);
                        if (ss == null) continue;
                        Integer sp = ss.getPoints() == null ? 0 : ss.getPoints();
                        totalPoints += sp;
                    }
                } else {
                    // añadir puntos de screening una sola vez: tomar la primera silla
                    Object first = seats.get(0);
                    java.lang.reflect.Method getSeatId = first.getClass().getMethod("getSeatId");
                    java.util.UUID seatId = (java.util.UUID) getSeatId.invoke(first);
                    SeatScreeningEntity ss = seatScreeningRepository.findById(seatId).orElse(null);
                    if (ss != null) {
                        Integer sp = ss.getPoints() == null ? 0 : ss.getPoints();
                        totalPoints += sp;
                    }
                }
            }

            if (totalPoints > 0) {
                addPoints(buyerId, totalPoints, "Puntos por compra automática");
            }
        } catch (Exception e){
            // no bloquear flujo principal
            System.out.printf("Advertencia: no fue posible calcular puntos automaticamente: %s", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void addPoints(UUID buyerId, int points, String description) {
        BuyerEntity buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new CinePachoException("Buyer not found when adding points"));
        PointsGainedEntity record = PointsGainedEntity.builder()
                .buyer(buyer)
                .points(points)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        pointsGainedRepository.save(record);

        Integer current = buyer.getPoints() == null ? 0 : buyer.getPoints();
        buyer.setPoints(current + points);
        buyerRepository.save(buyer);
    }

    @Override
    public Integer getPoints(UUID buyerId) {
        BuyerEntity buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new CinePachoException("Buyer not found"));
        return buyer.getPoints() == null ? 0 : buyer.getPoints();
    }

    @Override
    public List<PointsRecordDTO> getPointsHistory(UUID buyerId) {
        List<PointsGainedEntity> list = pointsGainedRepository.findAllByBuyer_BuyerIdOrderByCreatedAtDesc(buyerId);
        return list.stream()
                .map(e -> new PointsRecordDTO(e.getPoints(), e.getDescription(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VoucherDTO redeemVoucher(UUID buyerId) throws Exception {
        BuyerEntity buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new CinePachoException("Buyer not found"));
        Integer current = buyer.getPoints() == null ? 0 : buyer.getPoints();
        if (current < 100) {
            throw new CinePachoException("Puntos insuficientes para redimir");
        }
        buyer.setPoints(current - 100);
        buyerRepository.save(buyer);

        VoucherEntity v = VoucherEntity.builder()
                .code(UUID.randomUUID().toString())
                .buyer(buyer)
                .createdAt(LocalDateTime.now())
                .expiry(LocalDateTime.now().plusMonths(6))
                .used(false)
                .build();
        voucherRepository.save(v);

        return new VoucherDTO(v.getCode(), v.getExpiry(), v.isUsed());
    }

    @Override
    @Transactional
    public VoucherDTO validateVoucher(String code) throws Exception {
        VoucherEntity v = voucherRepository.findByCode(code)
                .orElseThrow(() -> new CinePachoException("Voucher no existe"));
        if (v.isUsed()) {
            throw new CinePachoException("Voucher ya fue usado");
        }
        if (v.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CinePachoException("Voucher vencido");
        }
        // Marcar como usado
        v.setUsed(true);
        voucherRepository.save(v);
        return new VoucherDTO(v.getCode(), v.getExpiry(), v.isUsed());
    }

    @Override
    @Transactional
    public void setSnackPoints(UUID snackId, Integer points) {
        SnackEntity snack = snackRepository.findById(snackId)
                .orElseThrow(() -> new CinePachoException("Snack no encontrado"));
        snack.setPoints(points);
        snackRepository.save(snack);
    }

    @Override
    @Transactional
    public void setSeatScreeningPoints(UUID seatScreeningId, Integer points) {
        SeatScreeningEntity ss = seatScreeningRepository.findById(seatScreeningId)
                .orElseThrow(() -> new CinePachoException("SeatScreening no encontrado"));
        ss.setPoints(points);
        seatScreeningRepository.save(ss);
    }

    @Override
    @Transactional
    public void setByUnitMode(boolean byUnit) {
        PointsConfigEntity cfg = pointsConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (cfg == null) {
            cfg = PointsConfigEntity.builder().byUnit(byUnit).build();
        } else {
            cfg.setByUnit(byUnit);
        }
        pointsConfigRepository.save(cfg);
    }

    @Override
    public boolean isByUnitMode() {
        PointsConfigEntity cfg = pointsConfigRepository.findTopByOrderByIdDesc().orElse(null);
        return cfg == null || cfg.isByUnit();
    }
}