package CinePacho.demo.auth.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class BuyerManagerImpl implements BuyerManager {

    private final BuyerRepository buyerRepository;


    public BuyerManagerImpl(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @Override
    public BuyerEntity getBuyerById(UUID id) {
        try {
            return buyerRepository.findByBuyerId(id);
        }catch (CinePachoException e){
            throw new CinePachoException("Buyer not found with id :" + id + " " + e.getMessage());
        }
    }

    @Override
    public BuyerEntity getBuyerByEmail(String email) {
        return buyerRepository.getBuyerByEmail(email)
                .orElseThrow(() -> new CinePachoException("Buyer not found with email: " + email));
    }

    @Override
    public void addWatchedMovie(UUID buyerId, Long movieId) {
        BuyerEntity buyer = getBuyerById(buyerId);
        if (buyer.getWatchedMovieIds() != null && !buyer.getWatchedMovieIds().contains(movieId)) {
            buyer.getWatchedMovieIds().add(movieId);
            buyerRepository.save(buyer);
        } else if (buyer.getWatchedMovieIds() == null) {
            buyer.setWatchedMovieIds(new ArrayList<>());
            buyer.getWatchedMovieIds().add(movieId);
            buyerRepository.save(buyer);
        }
    }
}
