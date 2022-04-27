package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.stereotype.Component;

@Component
public class Oauth2AuthorisationStrategy implements AuthorisationStrategy {
    @Override
    public boolean authorise(Caller caller) {
        return caller.inAuthorisedRole("chs-order-investigator");
    }

    @Override
    public IdentityType identityType() {
        return IdentityType.OAUTH2;
    }
}
