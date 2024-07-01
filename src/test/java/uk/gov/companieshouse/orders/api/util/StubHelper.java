package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;
import uk.gov.companieshouse.orders.api.model.*;

public final class StubHelper {
    private StubHelper() {
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, OffsetDateTime creationDate, PaymentStatus paymentStatus) {
        final Checkout checkout = new Checkout();
        checkout.setId(checkoutId);
        checkout.setUserId(ERIC_IDENTITY_VALUE);
        checkout.setCreatedAt(creationDate);

        final CheckoutData checkoutData = new CheckoutData();
        checkoutData.setReference(checkoutId);
        checkoutData.setTotalOrderCost("100");
        checkoutData.setCheckedOutBy(new ActionedBy());
        checkoutData.getCheckedOutBy().setEmail(email);
        checkoutData.setItems(Collections.singletonList(new Item()));
        checkoutData.getItems().get(0).setId("item-id-123");
        checkoutData.getItems().get(0).setKind("item#certificate");
        checkoutData.getItems().get(0).setCompanyNumber(companyNumber);
        checkoutData.setLinks(new CheckoutLinks());
        checkoutData.getLinks().setSelf("http");
        checkoutData.setStatus(paymentStatus);

        checkout.setData(checkoutData);

        return checkout;
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber) {
        return getCheckout(checkoutId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay().atOffset(ZoneOffset.UTC), null);
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, OffsetDateTime creationDate) {
        return getCheckout(checkoutId, email, companyNumber, creationDate, null);
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, PaymentStatus paymentStatus) {
        return getCheckout(checkoutId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay().atOffset(ZoneOffset.UTC), paymentStatus);
    }

    public static Order getOrder(String orderId, List<Item> items, String email,
            OffsetDateTime creationDate) {
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
        orderData.setItems(items);
        orderData.setLinks(new OrderLinks());
        orderData.getLinks().setSelf("http");

        order.setData(orderData);

        return order;
    }

    public static Order getOrder(String orderId, String email, String companyNumber, OffsetDateTime creationDate) {
        return getOrder(orderId, Collections.singletonList(
                getOrderItem("item-id-123", "item#certificate", companyNumber)),
                email,
                creationDate);
    }

    public static Order getOrder(String orderId, String email, String companyNumber) {
        return getOrder(orderId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    public static Item getOrderItem(String id, String kind, String companyNumber) {
        Item orderItem = new Item();
        orderItem.setId(id);
        orderItem.setKind(kind);
        orderItem.setCompanyNumber(companyNumber);
        return orderItem;
    }

    public static Basket getBasket(String id) {
        final Basket basket = new Basket();
        basket.setId(id);
        basket.setCreatedAt(OffsetDateTime.of(2022, 7, 15, 11, 40, 0, 0, ZoneOffset.UTC));
        basket.setUpdatedAt(OffsetDateTime.of(2022, 7, 15, 11, 40, 0, 0, ZoneOffset.UTC));

        BasketData data = new BasketData();
        data.setEnrolled(true);

        Item item1 = new Item();
        item1.setItemUri("/orderable/certificate/123");

        Item item2 = new Item();
        item2.setItemUri("/orderable/certificate/456");

        data.setItems(Arrays.asList(item1, item2));
        basket.setData(data);

        return basket;
    }
}
