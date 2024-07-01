package uk.gov.companieshouse.orders.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class CheckoutSummary {
    private final String id;
    private final String email;
    private final String companyNumber;
    private final String productLine;
    private final OffsetDateTime orderDate;
    private final PaymentStatus paymentStatus;
    private final Links links;

    private CheckoutSummary(Builder builder) {
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

    public static final class Builder implements CheckoutSummaryBuildable {
        private String id;
        private String email;
        private String companyNumber;
        private String productLine;
        private OffsetDateTime orderDate;
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

        public Builder withCheckoutDate(OffsetDateTime orderDate) {
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

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getProductLine() {
        return productLine;
    }

    public OffsetDateTime getOrderDate() {
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
