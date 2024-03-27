package uk.gov.companieshouse.orders.api.model;

public class CheckoutCriteria {
    private final String orderId;
    private final String email;
    private final String companyNumber;

    private CheckoutCriteria(Builder builder) {
        orderId = builder.orderId;
        email = builder.email;
        companyNumber = builder.companyNumber;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String orderId;
        private String email;
        private String companyNumber;

        private Builder() {
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
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

        public CheckoutCriteria build() {
            return new CheckoutCriteria(this);
        }
    }

    public String getCheckoutId() {
        return orderId;
    }

    public String getEmail() {
        return email;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }
}
