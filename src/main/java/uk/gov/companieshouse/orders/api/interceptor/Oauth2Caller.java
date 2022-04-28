package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.StringUtils;

@Component
@RequestScope
class Oauth2Caller {
    private final HttpServletRequest request;
    private final Responder responder;
    private final StringUtils stringUtils;
    private boolean authorisedRole;

    Oauth2Caller(HttpServletRequest request, Responder responder, StringUtils stringUtils) {
        this.request = request;
        this.responder = responder;
        this.stringUtils = stringUtils;
    }

    Oauth2Caller checkAuthorisedRole(String role) {
        String authorisedRolesHeader = RequestUtils.getRequestHeader(request, "ERIC-Authorised-Roles");
        if (isNull(authorisedRolesHeader)) {
            responder.invalidate("Authentication error: caller authorised roles are absent");
            return this;
        }

        Set<String> authorisedRoles = stringUtils.asSet("\\s+", authorisedRolesHeader);
        if (! authorisedRoles.contains(role)) {
            responder.logMapPut(LoggingUtils.AUTHORISED_ROLES, authorisedRolesHeader)
                    .invalidate(String.format("Authentication error: caller is not in role %s", role));
            return this;
        }

        authorisedRole = true;
        return this;
    }

    boolean isAuthorisedRole() {
        return authorisedRole;
    }
}
