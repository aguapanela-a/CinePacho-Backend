package CinePacho.demo.points.controller;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.service.BuyerManagerImpl;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.PointsManager;
import CinePacho.demo.shared.auxiliaryClass.PointsRecordDTO;
import CinePacho.demo.shared.auxiliaryClass.VoucherDTO;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsManager pointsManager;
    private final JwtService jwtService;
    private final BuyerManagerImpl buyerManager;

    // Comprador consulta sus puntos e historial (unicamente ingresa BUYER)
    @GetMapping
    public ResponseEntity<?> getMyPoints(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        BuyerEntity buyer = buyerManager.getBuyerByEmail(email);
        Integer points = pointsManager.getPoints(buyer.getBuyerId());
        List<PointsRecordDTO> history = pointsManager.getPointsHistory(buyer.getBuyerId());
        return ResponseEntity.ok().body(new Object(){
            public final Integer pointsNow = points;
            public final List<PointsRecordDTO> historyPoints = history;
        });
    }

    // Comprador redime puntos por boleta (100 pts)
    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestHeader("Authorization") String auth) throws Exception {
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        BuyerEntity buyer = buyerManager.getBuyerByEmail(email);
        VoucherDTO voucher = pointsManager.redeemVoucher(buyer.getBuyerId());
        return ResponseEntity.ok(voucher);
    }

    // Empleado/Gerente: validar y consumir voucher en taquilla
    @PostMapping("/validate")
    public ResponseEntity<?> validateVoucher(@RequestBody Map<String, String> body) throws Exception {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new CinePachoException("El código es obligatorio");
        }
        VoucherDTO v = pointsManager.validateVoucher(code);
        return ResponseEntity.ok(v);
    }

    // ADMIN: toggle modo
    @PutMapping("/admin/mode")
    public ResponseEntity<?> setMode(@RequestParam boolean byUnit) {
        pointsManager.setByUnitMode(byUnit);
        return ResponseEntity.ok().body(new Object(){ public final String message = "Modo actualizado"; });
    }

    // ADMIN: asignar puntos a snack
    @PutMapping("/admin/snack/{snackId}/points")
    public ResponseEntity<?> setSnackPoints(@PathVariable UUID snackId, @RequestParam Integer points) {
        pointsManager.setSnackPoints(snackId, points);
        return ResponseEntity.ok().body(new Object(){ public final String message = "Puntos de snack actualizados"; });
    }

    // ADMIN: asignar puntos a seat screening
    @PutMapping("/admin/seat/{seatId}/points")
    public ResponseEntity<?> setSeatPoints(@PathVariable UUID seatId, @RequestParam Integer points) {
        pointsManager.setSeatScreeningPoints(seatId, points);
        return ResponseEntity.ok().body(new Object(){ public final String message = "Puntos de silla/función actualizados"; });
    }
}
