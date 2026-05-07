package CinePacho.demo.auth.entities.user;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Setter @Getter
@NoArgsConstructor
public class UserEntity implements UserDetails {

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

    @Column(nullable = false)
    private boolean enabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.userType.name().toUpperCase()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

}
