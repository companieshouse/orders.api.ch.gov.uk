package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_ROLES;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;
import uk.gov.companieshouse.orders.api.util.StringHelper;

@Component
@RequestScope
class Oauth2Authoriser {
    private final WebContext webContext;
    private final StringHelper stringHelper;
    private boolean hasPermission;

    Oauth2Authoriser(WebContext webContext, StringHelper stringHelper) {
        this.webContext = webContext;
        this.stringHelper = stringHelper;
    }

    Oauth2Authoriser checkPermission(String permission) {
        // Note: ERIC_AUTHORISED_ROLES contains a space separated list of permissions
        String authorisedRolesHeader = webContext.getHeader(ERIC_AUTHORISED_ROLES);
        if (isNull(authorisedRolesHeader)) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authorisation error: caller authorised roles are absent")
                    .build());
            return this;
        }

        // Note: permissions are space separated
        Set<String> permissions = stringHelper.asSet("\\s+", authorisedRolesHeader);
        if (! permissions.contains(permission)) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withLogMapPut(LoggingUtils.AUTHORISED_ROLES, authorisedRolesHeader)
                    .withMessage("Authorisation error: caller does not have permission %s", permission)
                    .build());
            return this;
        }

        hasPermission = true;
        return this;
    }

    boolean hasPermission() {
        return hasPermission;
    }
}
