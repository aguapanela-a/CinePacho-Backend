package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.response.BillingDTO;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.entities.BillingEntity;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.QrStatus;
import CinePacho.demo.payment.repository.BillingRepository;
import CinePacho.demo.shared.enumeration.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// billing/BillingService.java
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final QrGeneratorService qrGeneratorService;
    private final CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider employeeMultiplexProvider;
    private final CinePacho.demo.shared.auxiliaryClass.UserManager userManager;
    private final CinePacho.demo.shared.serviceSecurity.JwtService jwtService;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Crea la factura y genera el QR.
     * Se llama al final de checkoutProducts() cuando el pago queda PENDING.
     * El QR apunta al endpoint de validación del empleado.
     */
    public BillingEntity createBilling(PaymentEntity payment,
                                       BuyerEntity buyer,
                                       CheckoutSummaryResponse summary,
                                       MovieScreening screening) {

        BillingEntity billing = BillingEntity.builder()
                .payment(payment)
                .buyer(buyer)
                .qrStatus(QrStatus.NOT_SCANNED)
                .createdAt(LocalDateTime.now())
                // ← guarda todo lo que necesitarás después
                .totalSeats(summary.getTotalSeats())
                .totalSnacks(summary.getTotalSnacks())
                .totalPurchase(summary.getTotalPurchase())
                .roomNumber(screening.getRoom().getRoomNumber())
                .movieTitle(screening.getMovie().getOriginalTitle())
                .screeningDate(screening.getDateTime().toString())
                .screeningId(screening.getId())
                .multiplexId(screening.getRoom().getMultiplex().getId())
                .build();

        BillingEntity saved = billingRepository.save(billing);

        // El QR codifica la URL que el empleado escaneará
        // cuando escanee, el sistema valida y cambia el estado
        String qrContent = baseUrl + "/api/checkout/employee/billing/" + saved.getId() + "/scan";
        String qrBase64 = qrGeneratorService.generateQrBase64(qrContent, 300, 300);

        saved.setQrBase64(qrBase64);
        return billingRepository.save(saved);
    }

    /**
     * Valida el QR cuando el empleado lo escanea.
     * Solo permite el escaneo una vez.
     */
    public Map<String, String> scanQr(UUID billingId, String token) {

        BillingEntity billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new CinePachoException("Factura no encontrada"));

        // validar token y que el usuario sea empleado del multiplex de la factura
        String employeeEmail = jwtService.extractEmail(token);
        var user = userManager.getUserByEmail(employeeEmail);
        if (user.getUserType() != UserType.EMPLOYEE
                && user.getUserType() != UserType.MANAGER) {
            throw new CinePachoException("Solo el personal del multiplex puede escanear facturas");
        }

        UUID empMultiplex = employeeMultiplexProvider.getMultiplexIdByUserEmail(employeeEmail);
        if (!empMultiplex.equals(billing.getMultiplexId())) {
            throw new CinePachoException("Empleado no pertenece a este multiplex");
        }

        // Evita escaneos duplicados
        if (billing.getQrStatus() == QrStatus.SCANNED) {
            throw new CinePachoException("Esta entrada ya fue escaneada el "
                    + billing.getScannedAt());
        }

        billing.setQrStatus(QrStatus.SCANNED);
        billing.setScannedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        billingRepository.save(billing);

        return Map.of("message", "Entrada válida. Bienvenido a CinePacho");
    }

    public List<BillingDTO> getAllBillingsByUserId(UUID userId){
        // Obtener todas las facturas del usuario (userId corresponde al id de UserEntity)
        List<BillingEntity> billings = billingRepository.findAllByBuyer_User_UserIdOrderByCreatedAtDesc(userId);
        return billings.stream().map(billing -> {
            String status = billing.getPayment() != null && billing.getPayment().getStatus() != null
                    ? billing.getPayment().getStatus().name()
                    : "UNKNOWN";
            String message;
            switch (status) {
                case "COMPLETED":
                    message = "Pago completado";
                    break;
                case "PENDING":
                    message = "Pago pendiente";
                    break;
                case "FAILED":
                    message = "Pago fallido";
                    break;
                case "CANCELLED":
                    message = "Pago cancelado";
                    break;
                default:
                    message = "Estado desconocido";
            }

            return BillingDTO.builder()
                    .status(status)
                    .message(message)
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
        }).toList();
    }
}
