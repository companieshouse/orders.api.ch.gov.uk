package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CheckoutSummary {
    private final String id;
    private final String email;
    private final LocalDateTime orderDate;
    private final PaymentStatus paymentStatus;
    private final Links links;

    private CheckoutSummary(Builder builder) {
        id = builder.id;
        email = builder.email;
        orderDate = builder.orderDate;
        paymentStatus = builder.paymentStatus;
        links = builder.links;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String email;
        private LocalDateTime orderDate;
        private PaymentStatus paymentStatus;
        private Links links;

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

        public Builder withCheckoutDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder withPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public Builder withLinks(Links links) {
            this.links = links;
            return this;
        }

        public CheckoutSummary build() {
            return new CheckoutSummary(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Links getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutSummary that = (CheckoutSummary) o;
        return Objects.equals(id, that.id) && Objects.equals(email,
                that.email) && Objects.equals(orderDate,
                that.orderDate) && paymentStatus == that.paymentStatus && Objects.equals(
                links,
                that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                email,
                orderDate,
                paymentStatus,
                links);
    }
}
