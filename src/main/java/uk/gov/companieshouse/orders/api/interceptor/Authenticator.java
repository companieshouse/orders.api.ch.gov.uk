package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;

@Component
@RequestScope
class Authenticator {
    private final WebContext webContext;

    private boolean identityValid;
    private IdentityType identityType;

    Authenticator(WebContext webContext) {
        this.webContext = webContext;
    }

    Authenticator checkIdentity() {
        // Check identity provided
        if (isNull(webContext.getHeader(ERIC_IDENTITY))) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authentication error: no caller identity")
                    .build());
            return this;
        }

        // Check identity type provided
        String identityTypeHeader = webContext.getHeader(ERIC_IDENTITY_TYPE);
        if (isNull(identityTypeHeader)) {
            webContext.invalidate(LoggableBuilder.newBuilder()
                    .withMessage("Authentication error: no caller identity type")
                    .build());
            return this;
        }

        // Check identity type has an expected value
        identityType = IdentityType.getEnumValue(identityTypeHeader);
        if (isNull(this.identityType)) {
            webContext.invalidate(LoggableBuilder.newBuilder()
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
