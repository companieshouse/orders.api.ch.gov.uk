package uk.gov.companieshouse.orders.api.config;

public class FeatureOptions {

    private final boolean ordersSearchEndpointEnabled;
    private final boolean multiItemBasketSearchEnabled;

    public FeatureOptions(boolean ordersSearchEndpointEnabled, boolean multiItemBasketSearchEnabled) {
        this.ordersSearchEndpointEnabled = ordersSearchEndpointEnabled;
        this.multiItemBasketSearchEnabled = multiItemBasketSearchEnabled;
    }

    public boolean isOrdersSearchEndpointEnabled() {
        return ordersSearchEndpointEnabled;
    }

    public boolean isMultiItemBasketSearchEnabled() {
        return multiItemBasketSearchEnabled;
    }
}
