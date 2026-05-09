package CinePacho.demo.employeeManageer.repository;

import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends  JpaRepository<EmployeeEntity, UUID> {
    EmployeeEntity findByUser_Email(String email);

    EmployeeEntity findEmployeeEntityByUniqueCode(long uniqueCode);

    Optional<EmployeeEntity> findTopByOrderByUniqueCodeDesc();

}
