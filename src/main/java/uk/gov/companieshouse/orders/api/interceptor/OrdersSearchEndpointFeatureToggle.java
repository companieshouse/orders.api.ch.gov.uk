package uk.gov.companieshouse.orders.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;

@Component
public class OrdersSearchEndpointFeatureToggle implements HandlerInterceptor {
    private final FeatureOptions featureOptions;

    public OrdersSearchEndpointFeatureToggle(FeatureOptions featureOptions) {
        this.featureOptions = featureOptions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (! featureOptions.isOrdersSearchEndpointEnabled()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return false;
        }

        return true;
    }
}
