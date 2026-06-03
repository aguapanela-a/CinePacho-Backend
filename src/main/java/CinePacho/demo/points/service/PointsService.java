package CinePacho.demo.points.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.request.SeatSelectionRequest;
import CinePacho.demo.payment.dto.request.SnackSelectionRequest;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public void processPurchase(UUID buyerId, CheckoutRequest request) {
        if (request == null) return;

        try {
            PointsConfigEntity cfg = pointsConfigRepository.findTopByOrderByIdDesc().orElse(null);
            boolean byUnit = (cfg == null || cfg.isByUnit());

            int totalPoints = 0;

            // 1. Calcular puntos de los Snacks
            if (request.getSnacks() != null) {
                for (SnackSelectionRequest snackReq : request.getSnacks()) {
                    SnackEntity snack = snackRepository.findById(snackReq.getSnackId()).orElse(null);

                    if (snack != null && snack.getPoints() != null) {
                        int qty = (snackReq.getQuantity() != null) ? snackReq.getQuantity() : 0;

                        if (byUnit) {
                            totalPoints += (snack.getPoints() * qty);
                        } else if (qty > 0) {
                            totalPoints += snack.getPoints();
                        }
                    }
                }
            }

            // 2. Calcular puntos de las Sillas (Seats)
            if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                if (byUnit) {
                    for (SeatSelectionRequest seatReq : request.getSeats()) {
                        SeatScreeningEntity seatEntity = seatScreeningRepository.findById(seatReq.getSeatId()).orElse(null);

                        if (seatEntity != null && seatEntity.getPoints() != null) {
                            totalPoints += seatEntity.getPoints();
                        }
                    }
                } else {
                    SeatSelectionRequest firstSeat = request.getSeats().get(0);
                    SeatScreeningEntity seatEntity = seatScreeningRepository.findById(firstSeat.getSeatId()).orElse(null);

                    if (seatEntity != null && seatEntity.getPoints() != null) {
                        totalPoints += seatEntity.getPoints();
                    }
                }
            }

            // 3. Guardar los puntos en la base de datos
            if (totalPoints > 0) {
                addPoints(buyerId, totalPoints, "Puntos por compra automática");
            }

        } catch (Exception e) {
            System.err.println("Error al calcular puntos automáticamente: " + e.getMessage());
            e.printStackTrace();
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
        //si no tiene 100 puntos NO puede redimir
        if (current < 100) {
            throw new CinePachoException("Puntos insuficientes para redimir");
        }

        //si si tiene 100 o más genera el boucher
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
        if (v.getExpiry().isBefore(LocalDateTime.now(ZoneId.of("America/Bogota")))) {
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