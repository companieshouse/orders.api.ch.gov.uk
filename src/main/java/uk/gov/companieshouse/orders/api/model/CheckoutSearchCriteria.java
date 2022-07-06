package uk.gov.companieshouse.orders.api.model;

public class CheckoutSearchCriteria {
    private final CheckoutCriteria orderCriteria;
    private final PageCriteria pageCriteria;

    public CheckoutSearchCriteria(CheckoutCriteria orderCriteria, PageCriteria pageCriteria) {
        this.orderCriteria = orderCriteria;
        this.pageCriteria = pageCriteria;
    }

    public CheckoutCriteria getCheckoutCriteria() {
        return orderCriteria;
    }

    public PageCriteria getPageCriteria() {
        return pageCriteria;
    }
}
