package uk.gov.companieshouse.orders.api.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;
import uk.gov.companieshouse.orders.api.config.FeatureOptionsConfig;

/**
 * Enables API search order endpoint using feature options.
 *
 * @see OrderController#searchOrders(String, String, String, String)
 * @see FeatureOptions#isOrdersSearchEndpointEnabled()
 * @see FeatureOptionsConfig
 */
@Component
@Aspect
// TODO: rework as Filter implementation
public class EndpointEnabler {
    private final FeatureOptions featureOptions;

    public EndpointEnabler(FeatureOptions featureOptions) {
        this.featureOptions = featureOptions;
    }

    @Pointcut("execution(* uk.gov.companieshouse.orders.api.controller.OrderController.searchOrders(..))")
    void searchOrders() {
        // Intentionally empty
    }

    @Around("searchOrders()")
    Object searchOrders(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (featureOptions.isOrdersSearchEndpointEnabled()) {
            return proceedingJoinPoint.proceed();
        }
        return ResponseEntity.notFound().build();
    }
}
