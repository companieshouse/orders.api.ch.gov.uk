package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderSummary {
    private final String id;
    private final String email;
    private final String companyNumber;
    private final String productLine;
    private final LocalDateTime orderDate;
    private final PaymentStatus paymentStatus;
    private final Links links;

    private OrderSummary(Builder builder) {
        id = builder.id;
        email = builder.email;
        companyNumber = builder.companyNumber;
        productLine = builder.productLine;
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
        private String companyNumber;
        private String productLine;
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

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
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

        public Builder withPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public Builder withLinks(Links links) {
            this.links = links;
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

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getProductLine() {
        return productLine;
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
        OrderSummary that = (OrderSummary) o;
        return Objects.equals(id, that.id) && Objects.equals(email,
                that.email) && Objects.equals(companyNumber,
                that.companyNumber) && Objects.equals(productLine,
                that.productLine) && Objects.equals(orderDate,
                that.orderDate) && paymentStatus == that.paymentStatus && Objects.equals(
                links,
                that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                email,
                companyNumber,
                productLine,
                orderDate,
                paymentStatus,
                links);
    }
}
