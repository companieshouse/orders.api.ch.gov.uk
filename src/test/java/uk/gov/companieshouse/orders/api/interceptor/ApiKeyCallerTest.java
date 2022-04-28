package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
class ApiKeyCallerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private Responder responder;

    @Mock
    private StringHelper stringHelper;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private ApiKeyCaller caller;

    @DisplayName("Should fail authorisation if caller privileges absent")
    @Test
    void testAuthorisationFailsPrivilegesAbsent() {
        caller.checkAuthorisedKeyPrivilege("any");

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller privileges are absent"));
        assertThat(caller.isAuthorisedKeyPrivilege(), is(false));
    }

    @DisplayName("Should fail authorisation if wrong privilege is set")
    @Test
    void testAuthorisationFailsPrivilegeWrong() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("bad-privilege");
        caller.checkAuthorisedKeyPrivilege("internal-app");

        verify(responder).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller is without privilege internal-app"));
        assertThat(caller.isAuthorisedKeyPrivilege(), is(false));
    }

    @DisplayName("Should pass authorisation caller has internal app privilege")
    @Test
    void testAuthorisationPassesCorrectPrivilege() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");
        when(stringHelper.asSet(",", "internal-app"))
                .thenReturn(new HashSet<>(Collections.singletonList("internal-app")));
        caller.checkAuthorisedKeyPrivilege("internal-app");

        assertThat(caller.isAuthorisedKeyPrivilege(), is(true));
    }

    @DisplayName("Should pass authorisation if caller has asterisk privilege")
    @Test
    void testAuthorisationPassesPrivilegeAsterisk() {
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("*");
        when(stringHelper.asSet(",", "*"))
                .thenReturn(new HashSet<>(Collections.singletonList("*")));
        caller.checkAuthorisedKeyPrivilege("*");

        assertThat(caller.isAuthorisedKeyPrivilege(), is(true));
    }
}
