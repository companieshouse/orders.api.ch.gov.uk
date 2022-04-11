package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderSummary {
    private final String id;
    private final String email;
    private final String productLine;
    private final LocalDateTime orderDate;
    private final ResourceLink resourceLink;

    private OrderSummary(Builder builder) {
        id = builder.id;
        email = builder.email;
        productLine = builder.productLine;
        orderDate = builder.orderDate;
        resourceLink = builder.resourceLink;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String email;
        private String productLine;
        private LocalDateTime orderDate;
        private ResourceLink resourceLink;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withProductLine(String productLine) {
            this.productLine = productLine;
            return this;
        }

        public Builder withOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder withResourceLink(ResourceLink resourceLink) {
            this.resourceLink = resourceLink;
            return this;
        }

        public OrderSummary build() {
            return new OrderSummary(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getProductLine() {
        return productLine;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public ResourceLink getResourceLink() {
        return resourceLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderSummary that = (OrderSummary) o;
        return Objects.equals(id, that.id)
                && Objects.equals(email, that.email)
                && Objects.equals(productLine, that.productLine)
                && Objects.equals(orderDate, that.orderDate)
                && Objects.equals(resourceLink, that.resourceLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, productLine, orderDate, resourceLink);
    }
}
