package uk.gov.companieshouse.orders.api.model;

import java.time.OffsetDateTime;

public class OrderData extends AbstractOrderData {

    private OffsetDateTime orderedAt;

    private ActionedBy orderedBy;

    private OrderLinks links;

    public OffsetDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(OffsetDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public ActionedBy getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(ActionedBy orderedBy) {
        this.orderedBy = orderedBy;
    }

    public OrderLinks getLinks() {
        return links;
    }

    public void setLinks(OrderLinks links) {
        this.links = links;
    }
}
