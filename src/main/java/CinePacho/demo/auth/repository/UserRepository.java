package CinePacho.demo.auth.repository;


import CinePacho.demo.auth.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}
