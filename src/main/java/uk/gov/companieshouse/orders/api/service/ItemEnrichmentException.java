package uk.gov.companieshouse.orders.api.service;

/**
 * Thrown if an error occurs when attempting to enrich an item with its related item resource.
 */
public class ItemEnrichmentException extends RuntimeException {
    public ItemEnrichmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
