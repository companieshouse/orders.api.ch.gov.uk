package uk.gov.companieshouse.orders.api.model;

import java.lang.reflect.Proxy;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;

@Component
public class CheckoutSummaryBuilderFactory {

    private final FeatureOptions featureOptions;

    public CheckoutSummaryBuilderFactory(FeatureOptions featureOptions) {
        this.featureOptions = featureOptions;
    }

    public CheckoutSummaryBuildable newCheckoutSummaryBuilder() {
        CheckoutSummaryBuildable builder = CheckoutSummary.newBuilder();
        return (CheckoutSummaryBuildable) Proxy.newProxyInstance(
                builder.getClass().getClassLoader(),
                builder.getClass().getInterfaces(),
                new CheckoutSummaryInvocationHandler(this.featureOptions, builder));
    }
}
