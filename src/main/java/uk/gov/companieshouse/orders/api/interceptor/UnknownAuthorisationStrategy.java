package uk.gov.companieshouse.orders.api.interceptor;

public class UnknownAuthorisationStrategy implements AuthorisationStrategy {
    @Override
    public boolean authorise(Caller caller) {
        // Unknown caller identity type
        return false;
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.UNKNOWN;
    }
}
