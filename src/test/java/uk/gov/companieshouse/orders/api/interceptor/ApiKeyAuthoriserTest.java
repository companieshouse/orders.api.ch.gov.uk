package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
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
class ApiKeyAuthoriserTest {

    @Mock
    private WebContext webContext;

    @Mock
    private StringHelper stringHelper;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private ApiKeyAuthoriser authoriser;

    @DisplayName("Should fail authorisation if caller privileges absent")
    @Test
    void testAuthorisationFailsPrivilegesAbsent() {
        authoriser.checkAuthorisedKeyPrivilege("any");

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller privileges are absent"));
        assertThat(authoriser.isAuthorisedKeyPrivilege(), is(false));
    }

    @DisplayName("Should fail authorisation if wrong privilege is set")
    @Test
    void testAuthorisationFailsPrivilegeWrong() {
        when(webContext.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("bad-privilege");
        authoriser.checkAuthorisedKeyPrivilege("internal-app");

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller is without privilege internal-app"));
        assertThat(authoriser.isAuthorisedKeyPrivilege(), is(false));
    }

    @DisplayName("Should pass authorisation caller has internal app privilege")
    @Test
    void testAuthorisationPassesCorrectPrivilege() {
        when(webContext.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");
        when(stringHelper.asSet(",", "internal-app"))
                .thenReturn(new HashSet<>(Collections.singletonList("internal-app")));
        authoriser.checkAuthorisedKeyPrivilege("internal-app");

        assertThat(authoriser.isAuthorisedKeyPrivilege(), is(true));
    }

    @DisplayName("Should pass authorisation if caller has asterisk privilege")
    @Test
    void testAuthorisationPassesPrivilegeAsterisk() {
        when(webContext.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("*");
        when(stringHelper.asSet(",", "*"))
                .thenReturn(new HashSet<>(Collections.singletonList("*")));
        authoriser.checkAuthorisedKeyPrivilege("*");

        assertThat(authoriser.isAuthorisedKeyPrivilege(), is(true));
    }
}
