package CinePacho.demo.auth.entities.user;

import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.shared.enumeration.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;


    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Getter
    @Column(unique = true)
    private String email;

    @Getter
    @Column(nullable = false)
    private boolean enabled = false;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private EmployeeEntity employee;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.userType.name().toUpperCase()));
        
        // Si es employee, agregar su rol específico como autoridad
        if (this.userType == UserType.EMPLOYEE && this.employee != null) {
            authorities.add(new SimpleGrantedAuthority(this.employee.getRol().name().toUpperCase()));
        }
        
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public String getName() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
