package uk.gov.companieshouse.orders.api.config;

public class FeatureOptions {

    private final boolean ordersSearchEndpointEnabled;

    public FeatureOptions(boolean ordersSearchEndpointEnabled) {
        this.ordersSearchEndpointEnabled = ordersSearchEndpointEnabled;
    }

    public boolean isOrdersSearchEndpointEnabled() {
        return ordersSearchEndpointEnabled;
    }
}
