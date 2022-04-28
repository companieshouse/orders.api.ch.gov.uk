package uk.gov.companieshouse.orders.api.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@Component
@RequestScope
class Responder {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    private Map<String, Object> logMap = LoggingUtils.createLogMap();

    Responder(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    Responder logMapPut(String key, Object value) {
        logMap.put(key, value);
        return this;
    }

    Responder invalidate(String logMessage) {
        logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
        LOGGER.infoRequest(httpServletRequest, logMessage, logMap);
        httpServletResponse.setStatus(UNAUTHORIZED.value());
        return this;
    }
}
