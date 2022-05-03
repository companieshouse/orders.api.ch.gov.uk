package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorisationStrategyFactoryTest {

    @Mock
    private AuthorisationStrategy authorisationStrategy;

    @DisplayName("Should not find authorisation strategy if strategy not in factory")
    @Test
    void strategyNotInFactory() {
        AuthorisationStrategyFactory authorisationStrategyFactory = new AuthorisationStrategyFactory(
                Collections.emptySet());

        AuthorisationStrategy result = authorisationStrategyFactory.authorisationStrategy(IdentityType.KEY);

        assertThat(result, is(nullValue()));
    }

    @DisplayName("Should find authorisation strategy if strategy is in factory")
    @Test
    void findsStrategyInFactory() {
        when(authorisationStrategy.identityType()).thenReturn(IdentityType.OAUTH2);
        AuthorisationStrategyFactory authorisationStrategyFactory = new AuthorisationStrategyFactory(
                Stream.of(authorisationStrategy).collect(Collectors.toSet()));

        AuthorisationStrategy result = authorisationStrategyFactory.authorisationStrategy(IdentityType.OAUTH2);

        assertThat(result, is(notNullValue()));
        assertThat(result.identityType(), is(IdentityType.OAUTH2));
    }
}
