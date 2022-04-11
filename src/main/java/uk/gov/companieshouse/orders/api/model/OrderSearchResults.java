package uk.gov.companieshouse.orders.api.model;

import java.util.List;

public class OrderSearchResults {
    private final int totalOrders;
    private final List<OrderSummary> orderSummaries;

    public OrderSearchResults(int totalOrders, List<OrderSummary> orderSummaries) {
        this.totalOrders = totalOrders;
        this.orderSummaries = orderSummaries;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public List<OrderSummary> getOrderSummaries() {
        return orderSummaries;
    }
}
