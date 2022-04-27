package uk.gov.companieshouse.orders.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;

@Component
@RequestScope
public class SecurityManager {
    private boolean authenticated;
    private AuthorisationStrategy authorisationStrategy;

    private final AuthorisationStrategyFactory authStrategyFactory;
    private final Caller caller;

    @Autowired
    public SecurityManager(AuthorisationStrategyFactory authStrategyFactory, Caller caller) {
        this.authStrategyFactory = authStrategyFactory;
        this.caller = caller;
    }

    boolean authenticate(/* String endpointId */) {
        return (authenticated = caller.hasIdentity());
    }

    boolean authorise(/* String endPointId */) {
        if (! authenticated) {
            throw new ForbiddenException("Caller is unauthenticated");
        }

        return authStrategyFactory.authorisationStrategy(caller.getIdentityType()/* ,endPointId */).authorise(caller);
    }
}
