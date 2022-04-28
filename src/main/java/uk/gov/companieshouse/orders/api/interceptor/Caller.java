package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@Component
@RequestScope
public class Caller {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    private boolean identityValid;
    private String identity;
    private IdentityType identityType;

    public Caller(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }
    
    public boolean checkIdentity() {
        // Check identity provided
        this.identity = AuthorisationUtil.getAuthorisedIdentity(httpServletRequest);
        if (isNull(this.identity)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(httpServletRequest, "Authentication error: no caller identity", logMap);
            httpServletResponse.setStatus(UNAUTHORIZED.value());
            return identityValid = false;
        }

        // Check identity type provided
        String identityType = AuthorisationUtil.getAuthorisedIdentityType(httpServletRequest);
        if (isNull(identityType)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(httpServletRequest,"Authentication error: no caller identity type", logMap);
            httpServletResponse.setStatus(UNAUTHORIZED.value());
            return identityValid = false;
        }

        // Check identity type has an expected value
        this.identityType = IdentityType.getEnumValue(identityType);
        if (isNull(this.identityType)) {
            Map<String, Object> logMap = LoggingUtils.createLogMap();
            logMap.put(LoggingUtils.IDENTITY_TYPE, identityType);
            logMap.put(LoggingUtils.STATUS, UNAUTHORIZED);
            LOGGER.infoRequest(httpServletRequest,String.format("Authentication error: invalid caller identity type %s", identityType), logMap);
            httpServletResponse.setStatus(UNAUTHORIZED.value());
            return identityValid = false;
        }

        return identityValid = true;
    }

    public boolean isIdentityValid() {
        return identityValid;
    }
    
    public IdentityType getIdentityType() {
        return identityType;
    }
}
