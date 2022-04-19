package uk.gov.companieshouse.orders.api.model;

public class OrderSearchCriteria {
    private final OrderCriteria orderCriteria;

    public OrderSearchCriteria(OrderCriteria orderCriteria) {
        this.orderCriteria = orderCriteria;
    }

    public OrderCriteria getOrderCriteria() {
        return orderCriteria;
    }
}
