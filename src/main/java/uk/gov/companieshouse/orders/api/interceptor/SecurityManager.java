package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;

@Component
public class SecurityManager {
    private final AuthorisationStrategyFactory authStrategyFactory;
    private final Caller caller;

    @Autowired
    public SecurityManager(AuthorisationStrategyFactory authStrategyFactory, Caller caller) {
        this.authStrategyFactory = authStrategyFactory;
        this.caller = caller;
    }

    boolean checkIdentity() {
        return caller.checkIdentity();
    }

    boolean checkPermission() {
        if (! caller.isIdentityValid()) {
            throw new ForbiddenException("Caller is unauthenticated");
        }

        return authStrategyFactory.authorisationStrategy(caller.getIdentityType()).authorise();
    }
}
