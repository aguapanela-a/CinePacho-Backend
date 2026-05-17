package CinePacho.demo.multiplex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;

import java.util.UUID;

public interface MultiplexRepository extends JpaRepository<MultiplexEntity, UUID> {
    boolean existsByNameAndCity(String name, String city);
}
