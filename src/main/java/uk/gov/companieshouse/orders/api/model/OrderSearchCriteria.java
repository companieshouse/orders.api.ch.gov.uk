package uk.gov.companieshouse.orders.api.model;

public class OrderSearchCriteria {
    private final OrderCriteria orderCriteria;
    private final PageCriteria pageCriteria;

    public OrderSearchCriteria(OrderCriteria orderCriteria, PageCriteria pageCriteria) {
        this.orderCriteria = orderCriteria;
        this.pageCriteria = pageCriteria;
    }

    public OrderCriteria getOrderCriteria() {
        return orderCriteria;
    }

    public PageCriteria getPageCriteria() {
        return pageCriteria;
    }
}
