package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_KEY_PRIVILEGES;

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
class ApiKeyCaller {
    private final HttpServletRequest request;
    private final Responder responder;
    private final StringHelper stringHelper;
    private boolean authorisedKeyPrivilege;

    ApiKeyCaller(HttpServletRequest request, Responder responder, StringHelper stringHelper) {
        this.request = request;
        this.responder = responder;
        this.stringHelper = stringHelper;
    }

    ApiKeyCaller checkAuthorisedKeyPrivilege(String privilege) {
        String privilegeList = RequestUtils.getRequestHeader(request, ERIC_AUTHORISED_KEY_PRIVILEGES);
        if (isNull(privilegeList)) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authorisation error: caller privileges are absent")
                    .build());
            return this;
        }

        // Note: authorised key privileges are comma separated
        Set<String> privileges = stringHelper.asSet(",", privilegeList);
        if (! (privileges.contains(privilege) || privileges.contains("*"))) {
            responder.invalidate(LoggableBuilder.newBuilder()
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
