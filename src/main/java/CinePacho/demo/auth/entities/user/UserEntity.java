package CinePacho.demo.auth.entities.user;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Setter @Getter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(unique = true)
    private String email;

    private boolean enabled = false;

}
