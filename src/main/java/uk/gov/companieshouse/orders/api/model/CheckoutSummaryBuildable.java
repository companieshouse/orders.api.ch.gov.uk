package uk.gov.companieshouse.orders.api.model;

import java.time.OffsetDateTime;

public interface CheckoutSummaryBuildable {
    CheckoutSummaryBuildable withId(String id);
    CheckoutSummaryBuildable withEmail(String email);
    CheckoutSummaryBuildable withCompanyNumber(String companyNumber);
    CheckoutSummaryBuildable withProductLine(String productLine);
    CheckoutSummaryBuildable withCheckoutDate(OffsetDateTime checkoutDate);
    CheckoutSummaryBuildable withPaymentStatus(PaymentStatus paymentStatus);
    CheckoutSummaryBuildable withLinks(Links links);
    CheckoutSummary build();
}
