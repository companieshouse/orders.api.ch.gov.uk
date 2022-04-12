package uk.gov.companieshouse.orders.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.util.CharEscaper;

@Component
class MongoSearchFieldMapper implements SearchFieldMapper {
    private static final String ANY_MATCH = ".*";
    private final CharEscaper charEscaper;

    public MongoSearchFieldMapper(CharEscaper charEscapER) {
        this.charEscaper = charEscapER;
    }

    @Override
    public String exactMatchOrAny(String field) {
        if (!StringUtils.isBlank(field)) {
            return "^" + charEscaper.escape(field).toString() + "$";
        }
        return ANY_MATCH;
    }

    @Override
    public String partialMatchOrAny(String field) {
        if (!StringUtils.isBlank(field)) {
            return charEscaper.escape(field).toString();
        }
        return ANY_MATCH;
    }
}
