package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;

@ExtendWith(MockitoExtension.class)
public class BasketServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 1, 12, 9, 1);

    @InjectMocks
    private BasketService service;

    @Mock
    private BasketRepository repository;

    private TimestampedEntityVerifier timestamps;

    @BeforeEach
    void setUp() {
        timestamps = new TimestampedEntityVerifier();
    }

    @Test
    public void saveBasketPopulatesCreatedAtAndUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);

        timestamps.start();

        service.saveBasket(basket);

        timestamps.end();
        timestamps.verifyCreationTimestampsWithinExecutionInterval(basket);
        verify(repository).save(basket);
    }

    @Test
    public void saveBasketPopulatesUpdatedAtAndSavesItem() {
        final Basket basket = new Basket();
        basket.setCreatedAt(CREATED_AT);
        basket.setId(ERIC_IDENTITY_VALUE);

        timestamps.start();

        service.saveBasket(basket);

        timestamps.end();
        assertThat(basket.getCreatedAt(), is(CREATED_AT));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(basket);
        verify(repository).save(basket);
    }

    @Test
    public void createBasketThrowsExceptionIfIdNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Basket basket = new Basket();
            service.saveBasket(basket);
        });
    }

    @DisplayName("test remove basket data item by basket id and item uri is successful")
    @Test
    void removeBasketDataItemByUri() {
        // given
        Basket basket = new Basket();
        Item item = new Item();
        item.setItemUri("123");
        basket.getItems().add(item);
        when(repository.removeBasketDataItemByUri("123","123")).thenReturn(basket);

        // when
        boolean result = service.removeBasketDataItemByUri("123", "123");
        // then
        assertTrue(result);
    }

    @DisplayName("test remove basket data item by basket id and item uri throws exception when uri not found")
    @Test
    void removeBasketDataItemByUriNotFound() {
        // given
        when(repository.removeBasketDataItemByUri("123","123")).thenReturn(new Basket());

        // when
        boolean result = service.removeBasketDataItemByUri("123", "123");
        // then
        assertFalse(result);
    }
}
