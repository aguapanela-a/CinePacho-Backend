package CinePacho.demo.auth.service;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
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
        return buyerRepository.findById(id)
                .orElseThrow(() -> new CinePachoException("Buyer not found"));
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
