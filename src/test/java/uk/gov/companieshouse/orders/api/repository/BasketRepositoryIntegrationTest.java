package uk.gov.companieshouse.orders.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.orders.api.config.AbstractMongoConfig;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.util.StubHelper;

@Testcontainers
@DataMongoTest
class BasketRepositoryIntegrationTest extends AbstractMongoConfig {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private BasketRepositoryImpl basketRepositoryImpl;

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @AfterEach
    void teardown() {
        basketRepository.deleteAll();
    }

    @DisplayName("test remove basket date item by basket id and item uri is successful")
    @Test
    void testRemoveItem() {
        // given
        Basket basket = StubHelper.getBasket("BASKET1");
        basketRepository.save(basket);

        // when
        Basket actual = basketRepositoryImpl.removeBasketDataItemByUri("BASKET1", "/orderable/certificate/123");

        // then
        assertEquals(basket.getId(), actual.getId());
        assertEquals(2, actual.getData().getItems().size());
        assertFalse(actual.getData().getItems().contains(basket.getData().getItems().get(0)));
    }
}