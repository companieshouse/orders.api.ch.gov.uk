package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;

@ExtendWith(MockitoExtension.class)
class SecurityManagerTest {

    @Mock
    private AuthorisationStrategy strategy;

    @Mock
    private AuthorisationStrategyFactory factory;

    @Mock
    private Authenticator authenticator;

    @InjectMocks
    private SecurityManager securityManager;

    @DisplayName("check permission throws programming exception when identity is in valid")
    @Test
    void testCheckPermissionException() throws ForbiddenException {
        when(authenticator.isIdentityValid()).thenReturn(false);

        Exception exception = assertThrows(ForbiddenException.class, () -> securityManager.checkPermission());
        assertThat(exception.getMessage(), is("Caller is unauthenticated"));
    }

    @DisplayName("check permission returns true if authorisation strategy authorised")
    @Test
    void testCheckPermissionReturnsTrue() throws ForbiddenException {
        when(authenticator.isIdentityValid()).thenReturn(true);
        when(authenticator.getIdentityType()).thenReturn(IdentityType.KEY);
        when(factory.authorisationStrategy(any())).thenReturn(strategy);
        when(strategy.authorise()).thenReturn(true);

        assertThat(securityManager.checkPermission(), is(true));
        verify(factory).authorisationStrategy(IdentityType.KEY);
    }

    @DisplayName("Check identity returns true for valid caller identity")
    @Test
    void testCheckIdentityTrue() {
        when(authenticator.checkIdentity()).thenReturn(authenticator);
        when(authenticator.isIdentityValid()).thenReturn(true);
        assertThat(securityManager.checkIdentity(), is(true));
    }

    @DisplayName("Check identity returns false for invalid caller identity")
    @Test
    void testCheckIdentityFalse() {
        when(authenticator.checkIdentity()).thenReturn(authenticator);
        when(authenticator.isIdentityValid()).thenReturn(false);
        assertThat(securityManager.checkIdentity(), is(false));
    }
}
