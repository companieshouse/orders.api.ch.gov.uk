package uk.gov.companieshouse.orders.api.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.Loggable;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;
import uk.gov.companieshouse.orders.api.util.Log;

@Component
@RequestScope
class Responder {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Log log;

    public Responder(HttpServletRequest request, HttpServletResponse response, Log log) {
        this.request = request;
        this.response = response;
        this.log = log;
    }

    void invalidate(Loggable loggable) {
        log.infoRequest(LoggableBuilder.newBuilder(loggable)
                .withLogMapPut(LoggingUtils.STATUS, UNAUTHORIZED)
                .withRequest(request)
                .build());
        response.setStatus(UNAUTHORIZED.value());
    }
}
