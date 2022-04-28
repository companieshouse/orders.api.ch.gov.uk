package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@Component
@RequestScope
public class AuthenticationCaller {
    private final HttpServletRequest httpServletRequest;
    private final Responder responder;

    private boolean identityValid;
    private String identity;
    private IdentityType identityType;

    public AuthenticationCaller(HttpServletRequest httpServletRequest, Responder responder) {
        this.httpServletRequest = httpServletRequest;
        this.responder = responder;
    }

    public AuthenticationCaller checkIdentity() {
        // Check identity provided
        this.identity = AuthorisationUtil.getAuthorisedIdentity(httpServletRequest);
        if (isNull(this.identity)) {
            responder.invalidate("Authentication error: no caller identity");
            return this;
        }

        // Check identity type provided
        String identityType = AuthorisationUtil.getAuthorisedIdentityType(httpServletRequest);
        if (isNull(identityType)) {
            responder.invalidate("Authentication error: no caller identity type");
            return this;
        }

        // Check identity type has an expected value
        this.identityType = IdentityType.getEnumValue(identityType);
        if (isNull(this.identityType)) {
            responder.logMapPut(LoggingUtils.IDENTITY_TYPE, identityType);
            responder.invalidate(String.format(
                    "Authentication error: invalid caller identity type %s",
                    identityType));
            return this;
        }

        identityValid = true;
        return this;
    }

    public boolean isIdentityValid() {
        return identityValid;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }
}
