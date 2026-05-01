package CinePacho.demo.client.entitites;

import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.shared.enumeration.UserType;
import jakarta.persistence.*;
import lombok.Generated;
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
