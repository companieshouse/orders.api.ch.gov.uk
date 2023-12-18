package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.util.Loggable;
import uk.gov.companieshouse.orders.api.util.Log;

@ExtendWith(MockitoExtension.class)
class WebContextTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Log log;

    @Mock
    private Loggable loggable;

    @Captor
    private ArgumentCaptor<Loggable> loggableArgumentCaptor;

    @InjectMocks
    private WebContext webContext;

    @DisplayName("Should correctly invalidate response")
    @Test
    void correctlyInvalidateResponse() {
        when(loggable.getMessage()).thenReturn("log-message");

        webContext.invalidate(loggable);

        verify(log).infoRequest(loggableArgumentCaptor.capture());
        assertThat(loggableArgumentCaptor.getValue().getMessage(), is("log-message"));
        assertThat(loggableArgumentCaptor.getValue().getRequest(), is(request));
        verify(response).setStatus(401);
    }

    @DisplayName("Should return an expected header value from web context")
    @Test
    void correctlyReturnsHeaderValue() {
        when(request.getHeader("some-header-key")).thenReturn("some-header-value");

        String actual = webContext.getHeader("some-header-key");

        assertThat(actual, is("some-header-value"));
    }
}
