package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Objects.nonNull;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.RequestUtils;

@Component
@RequestScope
public class Caller {
    private final HttpServletRequest request;

    public Caller(HttpServletRequest request) {
        this.request = request;
    }

    public boolean hasAuthorisedKeyPrivilege(String privilege) {
        Set<String> privileges = getAuthorisedKeyPrivileges();
        return privileges.contains(privilege) || privileges.contains("*");
    }

    public boolean hasIdentity() {
        return nonNull(getIdentity());
    }

    public boolean inAuthorisedRole(String role) {
        return getAuthorisedRoles().contains(role);
    }

    public IdentityType getIdentityType() {
        return IdentityType.getEnumValue(AuthorisationUtil.getAuthorisedIdentityType(request));
    }

    private Set<String> getAuthorisedKeyPrivileges() {
        // Comma separated list
        return headerAsSet("\\,", RequestUtils.getRequestHeader(request, "ERIC-Authorised-Key-Privileges"));
    }

    private Identity getIdentity() {
        return Optional.ofNullable(AuthorisationUtil.getAuthorisedIdentity(request))
                .map(id -> new Identity(id))
                .orElse(null);
    }

    private Set<String> getAuthorisedRoles() {
        // Space separated list
        return headerAsSet("\\s+", RequestUtils.getRequestHeader(request, "ERIC-Authorised-Roles"));
    }

    private Set<String> headerAsSet(String regex, String headerValue) {
        return Stream.of(Optional.ofNullable(headerValue)
                        .orElse("")
                        .split(regex))
                .filter(value -> StringUtils.isEmpty(value))
                .collect(Collectors.toSet());
    }
}
