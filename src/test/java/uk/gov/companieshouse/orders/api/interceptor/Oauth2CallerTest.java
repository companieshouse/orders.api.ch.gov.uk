package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
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
import uk.gov.companieshouse.orders.api.util.StringHelper;

@ExtendWith(MockitoExtension.class)
class Oauth2CallerTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private Responder responder;

    @Mock
    private StringHelper stringHelper;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private Oauth2Caller caller;

    @DisplayName("Should fail authorisation if caller roles are absent")
    @Test
    void authorisationFailsRolesAbsent() {

        caller.checkAuthorisedRole("any-role");

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller authorised roles are absent"));
        assertThat(caller.isAuthorisedRole(), is(false));
    }

    @DisplayName("Should fail authorisation if caller is not in role")
    @Test
    void authorisationFailsCallNotInRole() {

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("abc def");
        caller.checkAuthorisedRole("any-role");

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller is not in role any-role"));
        assertThat(caller.isAuthorisedRole(), is(false));
    }

    @DisplayName("Authorisation should succeed if caller has authorised role")
    @Test
    void authorisationSuccess() {

        when(request.getHeader("ERIC-Authorised-Roles")).thenReturn("abc def");
        when(stringHelper.asSet("\\s+", "abc def")).thenReturn(new HashSet<>(Arrays.asList("abc", "def")));
        caller.checkAuthorisedRole("def");

        assertThat(caller.isAuthorisedRole(), is(true));
    }
}
