package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
class Oauth2AuthoriserTest {
    @Mock
    private WebContext webContext;

    @Mock
    private StringHelper stringHelper;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private Oauth2Authoriser authoriser;

    @DisplayName("Should fail authorisation if caller roles are absent")
    @Test
    void authorisationFailsRolesAbsent() {

        authoriser.checkPermission("any-role");

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller authorised roles are absent"));
        assertThat(authoriser.hasPermission(), is(false));
    }

    @DisplayName("Should fail authorisation if caller is not in role")
    @Test
    void authorisationFailsCallNotInRole() {

        when(webContext.getHeader("ERIC-Authorised-Roles")).thenReturn("abc def");
        authoriser.checkPermission("any-permission");

        verify(webContext).invalidate(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("Authorisation error: caller does not have permission any-permission"));
        assertThat(authoriser.hasPermission(), is(false));
    }

    @DisplayName("Authorisation should succeed if caller has authorised role")
    @Test
    void authorisationSuccess() {

        when(webContext.getHeader("ERIC-Authorised-Roles")).thenReturn("abc def");
        when(stringHelper.asSet("\\s+", "abc def")).thenReturn(new HashSet<>(Arrays.asList("abc", "def")));
        authoriser.checkPermission("def");

        assertThat(authoriser.hasPermission(), is(true));
    }
}
