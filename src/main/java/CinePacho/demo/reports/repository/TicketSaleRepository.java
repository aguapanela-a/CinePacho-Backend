package CinePacho.demo.reports.repository;

import CinePacho.demo.reports.entities.TicketSaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para consultar ventas de tickets con detalle de multiplex y función.
 */
@Repository
public interface TicketSaleRepository extends JpaRepository<TicketSaleEntity, UUID> {

    // Carga ventas en rango con joins para evitar problemas de lazy loading en el reporte.
    @Query("""
            select ts
            from TicketSaleEntity ts
            join fetch ts.multiplex m
            join fetch ts.screening s
            join fetch s.movie mv
            where ts.soldAt >= :start and ts.soldAt <= :end
            """)
    List<TicketSaleEntity> findAllBySoldAtBetweenWithDetails(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
