package CinePacho.demo.snacks.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import CinePacho.demo.snacks.entities.SnackEntity;

import java.util.List;
import java.util.UUID;
 
@Repository
public interface SnackRepository extends JpaRepository<SnackEntity, UUID> {
    // Lista snacks con inventario disponible para la vista de compra
    //List<SnackEntity> findByQuantityGreaterThan(int quantity);

    List<SnackEntity> findByQuantityGreaterThanAndMultiplex_Id(int quantityIsGreaterThan, UUID multiplexId);
}
