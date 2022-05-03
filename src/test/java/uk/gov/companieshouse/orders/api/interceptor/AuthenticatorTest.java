package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class AuthenticatorTest {
    @Mock
    private WebContext webContext;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private Authenticator authenticator;

    @DisplayName("Should fail authentication when identity not present")
    @Test
    void testIdentityAbsent() {

        authenticator.checkIdentity();

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: no caller identity"));
        assertThat(authenticator.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type not present")
    @Test
    void testIdentityTypeAbsent() {

        when(webContext.getHeader("ERIC-Identity")).thenReturn("identity");
        authenticator.checkIdentity();

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: no caller identity type"));

        assertThat(authenticator.isIdentityValid(), is(false));
    }

    @DisplayName("Should fail authentication when identity type is not an expected value")
    @Test
    void testIdentityTypeValue() {

        when(webContext.getHeader("ERIC-Identity")).thenReturn("identity");
        when(webContext.getHeader("ERIC-Identity-Type")).thenReturn("bad-identity-type");
        authenticator.checkIdentity();

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authentication error: invalid caller identity type bad-identity-type"));
        assertThat(authenticator.isIdentityValid(), is(false));
    }

    @DisplayName("Should pass authentication when all required headers are set correctly")
    @Test
    void testAuthenticationSuccess() {

        when(webContext.getHeader("ERIC-Identity")).thenReturn("identity");
        when(webContext.getHeader("ERIC-Identity-Type")).thenReturn("key");
        authenticator.checkIdentity();

        assertThat(authenticator.isIdentityValid(), is(true));
        assertThat(authenticator.getIdentityType(), is(IdentityType.KEY));
    }
}
