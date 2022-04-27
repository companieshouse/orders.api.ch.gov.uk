package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
public class KeyAuthorisationStrategy implements AuthorisationStrategy {
    @Override
    public boolean authorise(Caller caller) {
        return caller.hasAuthorisedKeyPrivilege("internal-app");
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.KEY;
    }
}
