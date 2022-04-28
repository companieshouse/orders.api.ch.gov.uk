package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthenticationCallerTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private Responder responder;

    @InjectMocks
    private AuthenticationCaller authenticationCaller;

    @DisplayName("Should fail authentication when identity not present")
    @Test
    void testIdentityAbsent() {

        authenticationCaller.checkIdentity();

        verify(responder).invalidate("Authentication error: no caller identity");
        assertThat(authenticationCaller.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type not present")
    @Test
    void testIdentityTypeAbsent() {

        when(request.getHeader("ERIC-Identity")).thenReturn("identity");
        authenticationCaller.checkIdentity();

        verify(responder).invalidate("Authentication error: no caller identity type");
        assertThat(authenticationCaller.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type is not an expected value")
    @Test
    void testIdentityTypeValue() {

        when(request.getHeader("ERIC-Identity")).thenReturn("identity");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("bad-identity-type");
        authenticationCaller.checkIdentity();

        verify(responder).invalidate("Authentication error: invalid caller identity type bad-identity-type");
        verify(responder).logMapPut("identity_type", "bad-identity-type");
        assertThat(authenticationCaller.isIdentityValid(), is(false));
    }

    @DisplayName("Should pass authentication when all required headers are set correctly")
    @Test
    void testAuthenticationSuccess() {

        when(request.getHeader("ERIC-Identity")).thenReturn("identity");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("key");
        authenticationCaller.checkIdentity();

        assertThat(authenticationCaller.isIdentityValid(), is(true));
        assertThat(authenticationCaller.getIdentityType(), is(IdentityType.KEY));
    }
}
