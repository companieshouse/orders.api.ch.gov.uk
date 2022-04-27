package uk.gov.companieshouse.orders.api.interceptor;

public class Identity {
    private final String id;

    public Identity(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
