package CinePacho.demo.reports.repository;

import CinePacho.demo.reports.entities.SnackSaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para consultar ventas de snacks con detalle de multiplex y snack.
 */
@Repository
public interface SnackSaleRepository extends JpaRepository<SnackSaleEntity, UUID> {

    // Carga ventas en rango con joins para evitar problemas de lazy loading en el reporte.
    @Query("""
            select ss
            from SnackSaleEntity ss
            join fetch ss.multiplex m
            join fetch ss.snack s
            where ss.soldAt >= :start and ss.soldAt <= :end
            """)
    List<SnackSaleEntity> findAllBySoldAtBetweenWithDetails(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
