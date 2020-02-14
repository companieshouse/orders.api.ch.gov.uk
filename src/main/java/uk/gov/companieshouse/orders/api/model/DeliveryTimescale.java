package uk.gov.companieshouse.orders.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

import static uk.gov.companieshouse.orders.api.converter.EnumValueNameConverter.convertEnumValueNameToJson;

public enum DeliveryTimescale {
    STANDARD,
    SAME_DAY;

    @JsonValue
    public String getJsonName() {
        return convertEnumValueNameToJson(this);
    }
}
