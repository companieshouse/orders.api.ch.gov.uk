package uk.gov.companieshouse.orders.api.model;

import java.time.OffsetDateTime;

/**
 * Represents objects bearing created at and updated at timestamp properties.
 */
public interface TimestampedEntity {

   OffsetDateTime getCreatedAt();

   OffsetDateTime getUpdatedAt();

}
