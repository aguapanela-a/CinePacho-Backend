package CinePacho.demo.reports.entities;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.snacks.entities.SnackEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que registra cada venta de snacks para reportes mensuales.
 */
@Entity
@Table(name = "snack_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackSaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Multiplex donde se realizó la venta del snack.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplex_id", nullable = false)
    private MultiplexEntity multiplex;

    // Snack asociado a la venta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snack_id", nullable = false)
    private SnackEntity snack;

    // Fecha y hora exacta de la venta del snack.
    @Column(name = "sold_at", nullable = false)
    private LocalDateTime soldAt;

    // Cantidad de snacks vendidos en esta transacción.
    @Column(name = "snacks_quantity", nullable = false)
    private Integer snacksQuantity;

    // Total monetario de los snacks vendidos.
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}
