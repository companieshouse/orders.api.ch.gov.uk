package uk.gov.companieshouse.orders.api.service;

public interface SearchFieldMapper {
    String exactMatchOrAny(String field);
    String partialMatchOrAny(String field);
}
