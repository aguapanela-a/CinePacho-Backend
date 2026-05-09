package CinePacho.demo.employeeManageer.entities;

import CinePacho.demo.auth.entities.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

    @Column(nullable = false, unique = true)
    private Long uniqueCode;

    @Column(unique = true)
    private LocalDateTime startDate;

    @Column(unique = true)
    private String identityCard;

    private String position;

    @PositiveOrZero
    private BigDecimal salary;

    private String phoneNumber;
}
