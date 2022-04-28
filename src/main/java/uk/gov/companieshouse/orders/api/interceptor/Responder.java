package uk.gov.companieshouse.orders.api.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.Loggable;
import uk.gov.companieshouse.orders.api.util.Logger;

@Component
@RequestScope
class Responder {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Logger logger;

    public Responder(HttpServletRequest request, HttpServletResponse response, Logger logger) {
        this.request = request;
        this.response = response;
        this.logger = logger;
    }

    void invalidate(Loggable loggable) {
        Map<String, Object> logMap = new HashMap<>(loggable.getLogMap());
        logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
        logger.infoRequest(request, loggable.getMessage(), logMap);
        response.setStatus(UNAUTHORIZED.value());
    }
}
