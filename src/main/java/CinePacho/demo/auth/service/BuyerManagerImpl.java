package CinePacho.demo.auth.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BuyerManagerImpl implements BuyerManager {

    private final BuyerRepository buyerRepository;

    public BuyerManagerImpl(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @Override
    public BuyerEntity getBuyerById(UUID id) {
        return buyerRepository.findById(id)
                .orElseThrow(() -> new CinePachoException("Buyer not found"));
    }

}
