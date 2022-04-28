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
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final Logger logger;

    public Responder(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Logger logger) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.logger = logger;
    }

    void invalidate(Loggable loggable) {
        Map<String, Object> logMap = new HashMap<>(loggable.getLogMap());
        logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
        logger.info(loggable.getMessage(), logMap);
        httpServletResponse.setStatus(UNAUTHORIZED.value());
    }
}
