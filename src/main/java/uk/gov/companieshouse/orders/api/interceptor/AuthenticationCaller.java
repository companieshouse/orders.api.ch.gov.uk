package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@Component
@RequestScope
class AuthenticationCaller {
    private final HttpServletRequest httpServletRequest;
    private final Responder responder;

    private boolean identityValid;
    private String identity;
    private IdentityType identityType;

    AuthenticationCaller(HttpServletRequest httpServletRequest, Responder responder) {
        this.httpServletRequest = httpServletRequest;
        this.responder = responder;
    }

    AuthenticationCaller checkIdentity() {
        // Check identity provided
        this.identity = AuthorisationUtil.getAuthorisedIdentity(httpServletRequest);
        if (isNull(this.identity)) {
            responder.invalidate("Authentication error: no caller identity");
            return this;
        }

        // Check identity type provided
        String identityTypeHeader = AuthorisationUtil.getAuthorisedIdentityType(httpServletRequest);
        if (isNull(identityTypeHeader)) {
            responder.invalidate("Authentication error: no caller identity type");
            return this;
        }

        // Check identity type has an expected value
        identityType = IdentityType.getEnumValue(identityTypeHeader);
        if (isNull(this.identityType)) {
            responder.logMapPut(LoggingUtils.IDENTITY_TYPE, identityTypeHeader);
            responder.invalidate(String.format(
                    "Authentication error: invalid caller identity type %s",
                    identityTypeHeader));
            return this;
        }

        identityValid = true;
        return this;
    }

    boolean isIdentityValid() {
        return identityValid;
    }

    IdentityType getIdentityType() {
        return identityType;
    }
}
