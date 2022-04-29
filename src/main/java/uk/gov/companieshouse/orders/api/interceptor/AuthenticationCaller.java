package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.RequestUtils;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;

@Component
@RequestScope
class AuthenticationCaller {
    private final HttpServletRequest httpServletRequest;
    private final Responder responder;

    private boolean identityValid;
    private IdentityType identityType;

    AuthenticationCaller(HttpServletRequest httpServletRequest, Responder responder) {
        this.httpServletRequest = httpServletRequest;
        this.responder = responder;
    }

    AuthenticationCaller checkIdentity() {
        // Check identity provided
        if (isNull(RequestUtils.getRequestHeader(httpServletRequest, ERIC_IDENTITY))) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authentication error: no caller identity")
                    .build());
            return this;
        }

        // Check identity type provided
        String identityTypeHeader = RequestUtils.getRequestHeader(httpServletRequest, ERIC_IDENTITY_TYPE);
        if (isNull(identityTypeHeader)) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authentication error: no caller identity type")
                    .build());
            return this;
        }

        // Check identity type has an expected value
        identityType = IdentityType.getEnumValue(identityTypeHeader);
        if (isNull(this.identityType)) {
            responder.invalidate(LoggableBuilder.newBuilder()
                    .withLogMapPut(LoggingUtils.IDENTITY_TYPE, identityTypeHeader)
                    .withMessage("Authentication error: invalid caller identity type %s", identityTypeHeader)
                    .build());
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
