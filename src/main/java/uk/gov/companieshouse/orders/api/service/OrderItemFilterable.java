package uk.gov.companieshouse.orders.api.service;

import java.util.Optional;
import uk.gov.companieshouse.orders.api.model.Item;

public interface OrderItemFilterable {
    Optional<Item> getOrderItem(String orderId, String itemId);
}
