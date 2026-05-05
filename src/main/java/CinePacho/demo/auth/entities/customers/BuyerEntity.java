package CinePacho.demo.auth.entities.customers;

import CinePacho.demo.auth.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "buyers")
@Getter @Setter
public class BuyerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID buyerId;

    @OneToOne
    @JoinColumn(name = "userId", unique = true)
    private UserEntity user;


    private long points;


}
