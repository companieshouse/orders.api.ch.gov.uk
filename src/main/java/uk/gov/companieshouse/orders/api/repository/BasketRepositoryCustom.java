package uk.gov.companieshouse.orders.api.repository;

import uk.gov.companieshouse.orders.api.model.Basket;

public interface BasketRepositoryCustom {
    Basket clearBasketDataById(String id);
    Basket removeBasketDataItemByUri(String id, String uri);
}
