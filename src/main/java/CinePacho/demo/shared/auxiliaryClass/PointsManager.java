package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.payment.dto.request.CheckoutRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// Interfaz para desacoplar la lógica de puntos del resto de módulos.
@Component
public interface PointsManager {

    // Procesa una compra y asigna puntos al comprador según la configuración actual.
    void processPurchase(UUID buyerId, CheckoutRequest checkoutRequest);

    // Agrega puntos puntuales a un comprador con una descripcion.
    void addPoints(UUID buyerId, int points, String description);

    // Consulta puntos actuales de un comprador
    Integer getPoints(UUID buyerId);

    // Retorna el historial de movimientos de puntos como DTO simple (usar implementacion concreta para detalles)
    List<PointsRecordDTO> getPointsHistory(UUID buyerId);

    // Redimir puntos (ej. 100 puntos -> voucher). Retorna un VoucherDTO con el codigo y la vigencia
    VoucherDTO redeemVoucher(UUID buyerId) throws Exception;

    // Validar y consumir un voucher por su código (empleado en taquilla)
    VoucherDTO validateVoucher(String code) throws Exception;

    // Configuracion administrativa: definir puntos de snack o seatScreening
    void setSnackPoints(UUID snackId, Integer points);
    void setSeatScreeningPoints(UUID seatScreeningId, Integer points);

    // Toggle modo de acumulación: true = by unit, false = by purchase
    void setByUnitMode(boolean byUnit);
    boolean isByUnitMode();
}