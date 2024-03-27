package uk.gov.companieshouse.orders.api.model;

import java.util.List;
import java.util.Objects;

public class CheckoutSearchResults {
    private final long totalOrders;
    private final List<CheckoutSummary> orderSummaries;

    public CheckoutSearchResults(long totalOrders, List<CheckoutSummary> orderSummaries) {
        this.totalOrders = totalOrders;
        this.orderSummaries = orderSummaries;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public List<CheckoutSummary> getOrderSummaries() {
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
        CheckoutSearchResults that = (CheckoutSearchResults) o;
        return totalOrders == that.totalOrders && Objects.equals(orderSummaries,
                that.orderSummaries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOrders, orderSummaries);
    }
}
