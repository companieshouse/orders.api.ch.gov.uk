package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
class Oauth2AuthorisationStrategy implements AuthorisationStrategy {
    private final Oauth2Authoriser authoriser;

    Oauth2AuthorisationStrategy(Oauth2Authoriser authoriser) {
        this.authoriser = authoriser;
    }

    @Override
    public boolean authorise() {
        return authoriser.checkPermission("/admin/chs-order-investigation").hasPermission();
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.OAUTH2;
    }
}
