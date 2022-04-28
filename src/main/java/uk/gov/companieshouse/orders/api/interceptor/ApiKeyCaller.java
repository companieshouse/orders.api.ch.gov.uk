package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.StringUtils;

@Component
@RequestScope
public class ApiKeyCaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public ApiKeyCaller(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public boolean checkAuthorisedKeyPrivilege(String privilege) {
        String privilegeList = RequestUtils.getRequestHeader(request, "ERIC-Authorised-Key-Privileges");
        if (isNull(privilegeList)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request, "Authentication error: caller privileges are absent", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }

        Set<String> privileges = StringUtils.asSet(",", privilegeList);
        if (! (privileges.contains(privilege) || privileges.contains("*"))) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.AUTHORISED_KEY_PRIVILEGES, privileges);
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request,
                    String.format("Authorisation error: caller is without privilege %s", privilege),
                    logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }

        return true;
    }
}
