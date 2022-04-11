package uk.gov.companieshouse.orders.api.model;

public class ResourceLink {
    private final HRef self;
    private final HRef order;

    public ResourceLink(HRef self, HRef order) {
        this.self = self;
        this.order = order;
    }

    public HRef getSelf() {
        return self;
    }

    public HRef getOrder() {
        return order;
    }
}
