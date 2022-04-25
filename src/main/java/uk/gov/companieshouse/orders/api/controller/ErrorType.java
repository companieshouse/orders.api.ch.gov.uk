package uk.gov.companieshouse.orders.api.controller;

public enum ErrorType {
    VALIDATION("ch:validation"),
    SERVICE("ch:service");

    private String type;

    private ErrorType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public String getType() {
        return type;
    }
}
