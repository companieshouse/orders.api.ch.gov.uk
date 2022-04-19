package uk.gov.companieshouse.orders.api.model;

import java.util.Objects;

public class Links {
    private final HRef self;
    private final HRef order;

    public Links(HRef self, HRef order) {
        this.self = self;
        this.order = order;
    }

    public HRef getSelf() {
        return self;
    }

    public HRef getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Links that = (Links) o;
        return Objects.equals(self, that.self) && Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, order);
    }
}
