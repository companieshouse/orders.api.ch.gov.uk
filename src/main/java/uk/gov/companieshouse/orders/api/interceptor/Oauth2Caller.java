package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.StringHelper;

@Component
@RequestScope
class Oauth2Caller {
    private final HttpServletRequest request;
    private final Responder responder;
    private final StringHelper stringHelper;
    private boolean authorisedRole;

    Oauth2Caller(HttpServletRequest request, Responder responder, StringHelper stringHelper) {
        this.request = request;
        this.responder = responder;
        this.stringHelper = stringHelper;
    }

    Oauth2Caller checkAuthorisedRole() {
        String authorisedRolesHeader = RequestUtils.getRequestHeader(request, "ERIC-Authorised-Roles");
        if (isNull(authorisedRolesHeader)) {
            responder.invalidate("Authentication error: caller authorised roles are absent");
            return this;
        }

        Set<String> authorisedRoles = stringHelper.asSet("\\s+", authorisedRolesHeader);
        if (! authorisedRoles.contains("chs-order-investigator")) {
            responder.logMapPut(LoggingUtils.AUTHORISED_ROLES, authorisedRolesHeader)
                    .invalidate(String.format("Authentication error: caller is not in role %s",
                            "chs-order-investigator"));
            return this;
        }

        authorisedRole = true;
        return this;
    }

    boolean isAuthorisedRole() {
        return authorisedRole;
    }
}
