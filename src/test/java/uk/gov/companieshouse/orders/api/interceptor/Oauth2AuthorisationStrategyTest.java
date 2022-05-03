package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
class Oauth2AuthorisationStrategyTest {

    @Mock
    private Oauth2Authorizer authoriser;

    @InjectMocks
    private Oauth2AuthorisationStrategy strategy;

    @DisplayName("Should authorise caller")
    @Test
    void testAuthorised() {

        when(authoriser.checkPermission(any())).thenReturn(authoriser);
        when(authoriser.hasPermission()).thenReturn(true);

        boolean actual = strategy.authorise();

        assertThat(actual, is(true));
        verify(authoriser).checkPermission("/admin/chs-order-investigation");
    }

    @DisplayName("Should not authorise caller")
    @Test
    void testUnauthorised() {

        when(authoriser.checkPermission(anyString())).thenReturn(authoriser);
        when(authoriser.hasPermission()).thenReturn(false);

        boolean actual = strategy.authorise();

        assertThat(actual, is(false));
        verify(authoriser).checkPermission("/admin/chs-order-investigation");
    }

    @DisplayName("identity type should return oauth2 identity type enum")
    @Test
    void testIdentityType() {
        assertThat(strategy.identityType(), is(IdentityType.OAUTH2));
    }
}
