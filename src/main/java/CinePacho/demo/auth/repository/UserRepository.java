package CinePacho.demo.auth.repository;


import CinePacho.demo.auth.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findUserEntityByUsername(String username);
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
