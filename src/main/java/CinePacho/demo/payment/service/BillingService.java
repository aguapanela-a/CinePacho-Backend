package CinePacho.demo.payment.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.entities.BillingEntity;
import CinePacho.demo.payment.entities.PaymentEntity;
import CinePacho.demo.payment.enumeration.QrStatus;
import CinePacho.demo.payment.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

// billing/BillingService.java
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final QrGeneratorService qrGeneratorService;

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
    public Map<String, String> scanQr(UUID billingId) {

        BillingEntity billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new CinePachoException("Factura no encontrada"));

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
}
