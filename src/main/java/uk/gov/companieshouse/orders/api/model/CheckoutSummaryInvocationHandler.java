package uk.gov.companieshouse.orders.api.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;
import uk.gov.companieshouse.orders.api.exception.ServiceException;

public class CheckoutSummaryInvocationHandler implements InvocationHandler {

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
                return proxy;
            } else if ("withProductLine".equals(method.getName())
                    && featureOptions.isMultiItemBasketSearchEnabled()) {
                return proxy;
            } else if (method.getName().startsWith("with")) {
                method.invoke(builder, args);
                return proxy;
            } else {
                return method.invoke(builder, args);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ServiceException("Error mapping checkout summary", e);
        }
    }
}
