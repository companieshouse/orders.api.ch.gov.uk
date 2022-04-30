package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;

@Component
class SecurityManager {
    private final AuthorisationStrategyFactory factory;
    private final Authenticator authenticator;

    @Autowired
    SecurityManager(AuthorisationStrategyFactory factory, Authenticator authenticator) {
        this.factory = factory;
        this.authenticator = authenticator;
    }

    boolean checkIdentity() {
        return authenticator.checkIdentity().isIdentityValid();
    }

    boolean checkPermission() {
        if (! authenticator.isIdentityValid()) {
            throw new ForbiddenException("Caller is unauthenticated");
        }

        return factory.authorisationStrategy(authenticator.getIdentityType()).authorise();
    }
}
