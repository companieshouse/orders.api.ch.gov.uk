package uk.gov.companieshouse.orders.api.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.util.Loggable;
import uk.gov.companieshouse.orders.api.util.Logger;

@ExtendWith(MockitoExtension.class)
class ResponderTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Logger logger;

    @Mock
    private Loggable loggable;

    @InjectMocks
    private Responder responder;

    @DisplayName("Should correctly invalidate response")
    @Test
    void correctlyInvalidateResponse() {
        when(loggable.getMessage()).thenReturn("log-message");

        responder.invalidate(loggable);

        Mockito.verify(logger).info(eq("log-message"), any());
        Mockito.verify(response).setStatus(401);
    }
}
