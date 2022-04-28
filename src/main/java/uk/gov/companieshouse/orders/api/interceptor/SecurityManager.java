package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;

@Component
class SecurityManager {
    private final AuthorisationStrategyFactory factory;
    private final AuthenticationCaller caller;

    @Autowired
    SecurityManager(AuthorisationStrategyFactory factory, AuthenticationCaller caller) {
        this.factory = factory;
        this.caller = caller;
    }

    boolean checkIdentity() {
        return caller.checkIdentity().isIdentityValid();
    }

    boolean checkPermission() {
        if (! caller.isIdentityValid()) {
            throw new ForbiddenException("Caller is unauthenticated");
        }

        return factory.authorisationStrategy(caller.getIdentityType()).authorise();
    }
}
