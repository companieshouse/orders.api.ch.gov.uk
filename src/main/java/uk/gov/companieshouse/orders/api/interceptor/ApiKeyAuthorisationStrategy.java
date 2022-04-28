package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthorisationStrategy implements AuthorisationStrategy {
    private final ApiKeyCaller caller;

    public ApiKeyAuthorisationStrategy(ApiKeyCaller caller) {
        this.caller = caller;
    }

    @Override
    public boolean authorise() {
        return caller.checkAuthorisedKeyPrivilege("internal-app").isAuthorisedKeyPrivilege();
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.KEY;
    }
}
