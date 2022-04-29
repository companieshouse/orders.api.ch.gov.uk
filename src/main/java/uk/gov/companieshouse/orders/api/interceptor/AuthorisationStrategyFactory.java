package uk.gov.companieshouse.orders.api.interceptor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
class AuthorisationStrategyFactory {

    private Map<IdentityType, AuthorisationStrategy> authStrategyMap;

    AuthorisationStrategyFactory(Set<AuthorisationStrategy> authStrategies) {
        authStrategyMap = new EnumMap<>(IdentityType.class);
        authStrategies.forEach(strategy -> authStrategyMap.put(strategy.identityType(), strategy));
    }

    AuthorisationStrategy authorisationStrategy(IdentityType identityType) {
        return authStrategyMap.get(identityType);
    }
}
