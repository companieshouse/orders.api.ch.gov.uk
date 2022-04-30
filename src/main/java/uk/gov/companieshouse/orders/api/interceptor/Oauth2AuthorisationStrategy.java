package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
class Oauth2AuthorisationStrategy implements AuthorisationStrategy {
    private final Oauth2Authorizer caller;

    Oauth2AuthorisationStrategy(Oauth2Authorizer caller) {
        this.caller = caller;
    }

    @Override
    public boolean authorise() {
        return caller.checkAuthorisedRole("chs-order-investigator").isAuthorisedRole();
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.OAUTH2;
    }
}
