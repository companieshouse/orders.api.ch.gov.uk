package uk.gov.companieshouse.orders.api.model;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;

public class CheckoutSummaryInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private final CheckoutSummaryBuildable builder;
    private final FeatureOptions featureOptions;

    public CheckoutSummaryInvocationHandler(FeatureOptions featureOptions, CheckoutSummaryBuildable builder) {
        this.builder = builder;
        this.featureOptions = featureOptions;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            if ("withCompanyNumber".equals(method.getName())
                    && featureOptions.isMultiItemBasketSearchEnabled()) {
                LOGGER.debug("Not mapping company number as multi-item basket search is enabled");
                return proxy;
            } else if ("withProductLine".equals(method.getName())
                    && featureOptions.isMultiItemBasketSearchEnabled()) {
                LOGGER.debug("Not mapping product line as multi-item basket search is enabled");
                return proxy;
            } else if (method.getName().startsWith("with")) {
                method.invoke(builder, args);
                return proxy;
            } else {
                return method.invoke(builder, args);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
