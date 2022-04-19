package uk.gov.companieshouse.orders.api.interceptor;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

@Service
public class RequestMapper {

    /**
     * Represents the requests identified by this.
     */
    private final List<RequestMappingInfo> knownRequests;

    public RequestMapper(final List<RequestMappingInfo> knownRequests) {
        this.knownRequests = knownRequests;
    }

    /**
     * Gets the request mapping found for the request provided.
     *
     * @param request the HTTP request to be authenticated
     * @return the mapping representing the request if it is to be handled, or <code>null</code> if not
     */
    RequestMappingInfo getRequestMapping(final HttpServletRequest request) {
        for (final RequestMappingInfo mapping : knownRequests) {
            final RequestMappingInfo match = mapping.getMatchingCondition(request);
            if (match != null) {
                return match;
            }
        }
        return null; // no match found
    }
}
