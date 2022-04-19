package uk.gov.companieshouse.orders.api.model;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderSearchResults that = (OrderSearchResults) o;
        return totalOrders == that.totalOrders && Objects.equals(orderSummaries,
                that.orderSummaries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOrders, orderSummaries);
    }
}
