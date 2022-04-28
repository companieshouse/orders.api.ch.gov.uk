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
public class Oauth2Caller {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public Oauth2Caller(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public boolean checkAuthorisedRole(String role) {
        String authorisedRolesHeader = RequestUtils.getRequestHeader(request, "ERIC-Authorised-Roles");
        if (isNull(authorisedRolesHeader)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request, "Authentication error: caller authorised roles are absent", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }

        Set<String> authorisedRoles = StringUtils.asSet("\\s+", authorisedRolesHeader);
        if (! authorisedRoles.contains(role)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.AUTHORISED_ROLES, authorisedRolesHeader);
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(request,
                    String.format("Authentication error: caller is not in role %s", role),
                    logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }

        return true;
    }
}
