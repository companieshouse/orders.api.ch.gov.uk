package uk.gov.companieshouse.orders.api.model;

public class PageCriteria {
    private final int pageSize;

    public PageCriteria(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }
}
