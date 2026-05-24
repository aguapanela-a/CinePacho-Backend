package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface
BuyerManager {
    BuyerEntity getBuyerById(UUID id);
    
}
