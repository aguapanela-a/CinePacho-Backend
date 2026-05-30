package CinePacho.demo.employeeManageer.entities;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.enumeration.RolEmployee;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplex_id", nullable = false)
    private MultiplexEntity multiplex; // Multiplex asignado al personal del cine

    @Column(nullable = false, unique = true)
    private Long uniqueCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(unique = true)
    private LocalDateTime startDate;

    @Column(unique = true)
    private String identityCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolEmployee rol;

    @PositiveOrZero
    private BigDecimal salary;

    private String phoneNumber;
}
