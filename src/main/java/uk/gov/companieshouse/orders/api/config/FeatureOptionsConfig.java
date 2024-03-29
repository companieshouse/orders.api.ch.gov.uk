package uk.gov.companieshouse.orders.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureOptionsConfig {

    @Value("${feature.options.ordersSearchEndpointEnabled:false}")
    private boolean ordersSearchEnabled;

    @Value("${feature.options.multiItemBasketSearchEnabled:false}")
    private boolean multiItemBasketSearchEnabled;

    @Bean
    public FeatureOptions featureOptions() {
        return new FeatureOptions(ordersSearchEnabled, multiItemBasketSearchEnabled);
    }
}
