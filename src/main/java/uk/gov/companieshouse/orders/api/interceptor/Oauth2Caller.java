package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_ROLES;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;
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

    Oauth2Caller checkAuthorisedRole(String role) {
        String authorisedRolesHeader = RequestUtils.getRequestHeader(request, ERIC_AUTHORISED_ROLES);
        if (isNull(authorisedRolesHeader)) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authorisation error: caller authorised roles are absent")
                    .build());
            return this;
        }

        // Note: authorised roles are space separated
        Set<String> authorisedRoles = stringHelper.asSet("\\s+", authorisedRolesHeader);
        if (! authorisedRoles.contains(role)) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withLogMapPut(LoggingUtils.AUTHORISED_ROLES, authorisedRolesHeader)
                    .withMessage("Authorisation error: caller is not in role %s", role)
                    .build());
            return this;
        }

        authorisedRole = true;
        return this;
    }

    boolean isAuthorisedRole() {
        return authorisedRole;
    }
}
