package CinePacho.demo.employeeManageer.entities;

import CinePacho.demo.auth.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID employeeId;

    @OneToOne
    @JoinColumn(name = "userId", unique = true)
    private UserEntity user;

    @Column(unique = true)
    private long uniqueCode;

    @Column(unique = true)
    private LocalDateTime startDate;

}
