package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthorisationStrategyTest {

    @Mock
    private ApiKeyAuthoriser authoriser;

    @InjectMocks
    private ApiKeyAuthorisationStrategy strategy;

    @Test
    @DisplayName("should authorise caller")
    void testAuthorised() {
        when(authoriser.checkAuthorisedKeyPrivilege(anyString())).thenReturn(authoriser);
        when(authoriser.isAuthorisedKeyPrivilege()).thenReturn(true);

        assertThat(strategy.authorise(), is(true));
        verify(authoriser).checkAuthorisedKeyPrivilege("internal-app");
    }

    @Test
    @DisplayName("should not authorise caller")
    void testUnauthorised() {
        when(authoriser.checkAuthorisedKeyPrivilege(anyString())).thenReturn(authoriser);
        when(authoriser.isAuthorisedKeyPrivilege()).thenReturn(false);

        assertThat(strategy.authorise(), is(false));
        verify(authoriser).checkAuthorisedKeyPrivilege("internal-app");
    }

    @Test
    @DisplayName("identity type should return key identity type enum")
    void testIdentityType() {
        assertThat(strategy.identityType(), is(IdentityType.KEY));
    }
}
