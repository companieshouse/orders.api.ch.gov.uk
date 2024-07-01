package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.OrderLinks;

public final class OrderHelper {
    private OrderHelper() {
    }

    public static Order getOrder(String orderId, String email, String companyNumber, OffsetDateTime creationDate) {
        final Order order = new Order();
        order.setId(orderId);
        order.setUserId(ERIC_IDENTITY_VALUE);
        order.setCreatedAt(creationDate);

        final OrderData orderData = new OrderData();
        orderData.setOrderedAt(creationDate);
        orderData.setReference(orderId);
        orderData.setTotalOrderCost("100");
        orderData.setOrderedBy(new ActionedBy());
        orderData.getOrderedBy().setEmail(email);
        orderData.setItems(Collections.singletonList(new Item()));
        orderData.getItems().get(0).setId("item-id-123");
        orderData.getItems().get(0).setKind("item#certificate");
        orderData.getItems().get(0).setCompanyNumber(companyNumber);
        orderData.setLinks(new OrderLinks());
        orderData.getLinks().setSelf("http");

        order.setData(orderData);

        return order;
    }

    public static Order getOrder(String orderId, String email, String companyNumber) {
        return getOrder(orderId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay());
    }
}
