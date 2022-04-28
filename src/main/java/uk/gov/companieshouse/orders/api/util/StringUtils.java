package uk.gov.companieshouse.orders.api.util;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtils {

    private StringUtils() {
    }

    public static Set<String> asSet(String regexDelim, String values) {
        return Optional.ofNullable(values)
                .map(v -> Stream.of(v.split(regexDelim)))
                .get()
                .collect(Collectors.toSet());
    }
}
