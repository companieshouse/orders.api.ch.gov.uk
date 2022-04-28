package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.StringUtils;

@Component
@RequestScope
class ApiKeyCaller {
    private final HttpServletRequest request;
    private final Responder responder;
    private boolean authorisedKeyPrivilege;

    ApiKeyCaller(HttpServletRequest request, Responder responder) {
        this.request = request;
        this.responder = responder;
    }

    ApiKeyCaller checkAuthorisedKeyPrivilege(String privilege) {
        String privilegeList = RequestUtils.getRequestHeader(request, "ERIC-Authorised-Key-Privileges");
        if (isNull(privilegeList)) {
            responder.invalidate("Authentication error: caller privileges are absent");
            return this;
        }

        Set<String> privileges = StringUtils.asSet(",", privilegeList);
        if (! (privileges.contains(privilege) || privileges.contains("*"))) {

            Map<String, Object> logMap = LoggingUtils.createLogMap();
            responder.logMapPut(LoggingUtils.AUTHORISED_KEY_PRIVILEGES, privileges);
            responder.invalidate(String.format("Authorisation error: caller is without privilege %s", privilege));
            return this;
        }

        authorisedKeyPrivilege = true;
        return this;
    }

    boolean isAuthorisedKeyPrivilege() {
        return authorisedKeyPrivilege;
    }
}
