package uk.gov.companieshouse.orders.api.interceptor;

public interface AuthorisationStrategy {
    boolean authorise();
    IdentityType identityType();
}
