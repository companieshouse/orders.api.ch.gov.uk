package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
class ApiKeyAuthorisationStrategy implements AuthorisationStrategy {
    private final ApiKeyAuthoriser authoriser;

    ApiKeyAuthorisationStrategy(ApiKeyAuthoriser authoriser) {
        this.authoriser = authoriser;
    }

    @Override
    public boolean authorise() {
        return authoriser.checkAuthorisedKeyPrivilege("internal-app").isAuthorisedKeyPrivilege();
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.KEY;
    }
}
