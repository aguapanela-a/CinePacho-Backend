package CinePacho.demo.auth.entities.customers.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactory;
import org.springframework.stereotype.Component;

@Component
public class BuyerFactory implements UserFactory<Object> {

    private final BuyerRepository buyerRepository;

    public BuyerFactory(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @Override
    public UserType getSupportedType() {
        return UserType.BUYER;
    }

    //crea una entidad concreta Buyer y completa sus campos que el User no trae
    @Override
    public void createSpecificEntity(UserEntity user, Object registrationData) {
        BuyerEntity buyer = new BuyerEntity();
        buyer.setUser(user);
        buyer.setPoints(0);
        buyer.setCorreo(user.getEmail());
        buyerRepository.save(buyer);
    }
}
