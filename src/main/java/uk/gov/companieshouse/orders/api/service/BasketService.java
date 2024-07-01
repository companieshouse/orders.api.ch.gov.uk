package uk.gov.companieshouse.orders.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class BasketService {

    private final BasketRepository repository;

    @Value("${basket.enrolled}")
    private boolean enrolled;

    public BasketService(BasketRepository repository) {
        this.repository = repository;
    }

    public Basket saveBasket(Basket basket) {
        final OffsetDateTime now = OffsetDateTime.now();
        if(basket.getId() == null) {
            throw new IllegalArgumentException("ID Must be present");
        }
        if(basket.getCreatedAt() == null) {
            basket.setCreatedAt(now);
        }
        basket.setUpdatedAt(now);

        if (enrolled) {
            basket.getData().setEnrolled(true);
        }

        return repository.save(basket);
    }

    public Optional<Basket> getBasketById(String id) {
        return repository.findById(id);
    }

    public Basket clearBasket(String id) {
        return repository.clearBasketDataById(id);
    }

    /**
     * Returns true only if the item uri existed in the list of items before the update.
     *
     * @param id basket id
     * @param uri item uri
     * @return True if the item uri was successfully removed. False if the item uri was not found
     */
    public boolean removeBasketDataItemByUri(String id, String uri) {
        Basket retrievedBasket = repository.removeBasketDataItemByUri(id, uri);
        return (retrievedBasket.getData().getItems().stream().anyMatch(item -> uri.equals(item.getItemUri())));
    }
}
