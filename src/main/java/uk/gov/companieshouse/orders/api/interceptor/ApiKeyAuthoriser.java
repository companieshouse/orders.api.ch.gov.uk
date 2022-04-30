package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_KEY_PRIVILEGES;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;
import uk.gov.companieshouse.orders.api.util.StringHelper;

@Component
@RequestScope
class ApiKeyAuthoriser {
    private final WebContext webContext;
    private final StringHelper stringHelper;
    private boolean authorisedKeyPrivilege;

    ApiKeyAuthoriser(WebContext webContext, StringHelper stringHelper) {
        this.webContext = webContext;
        this.stringHelper = stringHelper;
    }

    ApiKeyAuthoriser checkAuthorisedKeyPrivilege(String privilege) {
        String privilegeList = webContext.getHeader(ERIC_AUTHORISED_KEY_PRIVILEGES);
        if (isNull(privilegeList)) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authorisation error: caller privileges are absent")
                    .build());
            return this;
        }

        // Note: authorised key privileges are comma separated
        Set<String> privileges = stringHelper.asSet(",", privilegeList);
        if (! (privileges.contains(privilege) || privileges.contains("*"))) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withLogMapPut(LoggingUtils.AUTHORISED_KEY_PRIVILEGES, privileges)
                    .withMessage("Authorisation error: caller is without privilege %s", privilege)
                    .build());
            return this;
        }

        authorisedKeyPrivilege = true;
        return this;
    }

    boolean isAuthorisedKeyPrivilege() {
        return authorisedKeyPrivilege;
    }
}
