package uk.gov.companieshouse.orders.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.util.CharEscaper;

/**
 * Translates supplied values into a escaped Regex query string.
 *
 * @see CharEscaper
 */
@Component
class MongoSearchFieldMapper implements SearchFieldMapper {
    private static final String ANY_MATCH = ".*";
    private final CharEscaper charEscaper;

    public MongoSearchFieldMapper(CharEscaper charEscaper) {
        this.charEscaper = charEscaper;
    }

    /**
     * Translates the supplied text into an escaped Regex exact match query; if the supplied text is
     * blank (null or whitespace) a wild card query is produced.
     *
     * @param text to be translated
     * @return translated query string, or wild card
     */
    @Override
    public String exactMatchOrAny(String text) {
        if (!StringUtils.isBlank(text)) {
            return "^" + charEscaper.escape(text).toString() + "$";
        }
        return ANY_MATCH;
    }

    /**
     * Translates the supplied text into an escaped Regex partial match query; if the supplied text
     * is blank (null or whitespace) a wild card query is produced.
     *
     * @param text to be translated
     * @return translated query string
     */
    @Override
    public String partialMatchOrAny(String text) {
        if (!StringUtils.isBlank(text)) {
            return charEscaper.escape(text).toString();
        }
        return ANY_MATCH;
    }
}
