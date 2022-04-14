package uk.gov.companieshouse.orders.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureOptionsConfig {

    @Value("${feature.options.ordersSearchEndpointEnabled}")
    private boolean ordersSearchEnabled;

    @Bean
    public FeatureOptions featureOptions() {
        return new FeatureOptions(ordersSearchEnabled);
    }
}
