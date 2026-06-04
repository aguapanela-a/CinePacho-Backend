package CinePacho.demo.payment.entities;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.payment.enumeration.QrStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
//hola
// billing/entities/BillingEntity.java
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "billings")
public class BillingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private BuyerEntity buyer;

    @Column(columnDefinition = "TEXT")
    private String qrBase64;

    @Enumerated(EnumType.STRING)
    private QrStatus qrStatus;

    private LocalDateTime createdAt;
    private LocalDateTime scannedAt;

    private BigDecimal totalSeats;
    private BigDecimal totalSnacks;
    private BigDecimal totalPurchase;
    private String roomNumber;
    private String movieTitle;
    private String screeningDate;

    // Para validaciones del empleado al escanear el QR
    private java.util.UUID screeningId;
    private java.util.UUID multiplexId;
}
