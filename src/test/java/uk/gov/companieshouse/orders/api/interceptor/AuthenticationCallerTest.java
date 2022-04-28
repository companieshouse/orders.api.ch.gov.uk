package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.util.Loggable;

@ExtendWith(MockitoExtension.class)
class AuthenticationCallerTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private Responder responder;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private AuthenticationCaller authenticationCaller;

    @DisplayName("Should fail authentication when identity not present")
    @Test
    void testIdentityAbsent() {

        authenticationCaller.checkIdentity();

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: no caller identity"));
        assertThat(authenticationCaller.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type not present")
    @Test
    void testIdentityTypeAbsent() {

        when(request.getHeader("ERIC-Identity")).thenReturn("identity");
        authenticationCaller.checkIdentity();

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: no caller identity type"));

        assertThat(authenticationCaller.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type is not an expected value")
    @Test
    void testIdentityTypeValue() {

        when(request.getHeader("ERIC-Identity")).thenReturn("identity");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("bad-identity-type");
        authenticationCaller.checkIdentity();

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: invalid caller identity type bad-identity-type"));
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
