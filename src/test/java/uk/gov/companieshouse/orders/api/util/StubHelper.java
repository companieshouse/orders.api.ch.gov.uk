package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import uk.gov.companieshouse.orders.api.model.*;

public final class StubHelper {
    private StubHelper() {
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, LocalDateTime creationDate, PaymentStatus paymentStatus) {
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
        return getCheckout(checkoutId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay(), null);
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, LocalDateTime creationDate) {
        return getCheckout(checkoutId, email, companyNumber, creationDate, null);
    }

    public static Checkout getCheckout(String checkoutId, String email, String companyNumber, PaymentStatus paymentStatus) {
        return getCheckout(checkoutId, email, companyNumber, LocalDate.of(2022, 4, 12).atStartOfDay(), paymentStatus);
    }

    public static Order getOrder(String orderId, String email, String companyNumber, LocalDateTime creationDate) {
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
