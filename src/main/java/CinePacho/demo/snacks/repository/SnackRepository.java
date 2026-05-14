package CinePacho.demo.snacks.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import CinePacho.demo.snacks.entities.SnackEntity;

import java.util.UUID;
 
@Repository
public interface SnackRepository extends JpaRepository<SnackEntity, UUID> {
    
}