package uk.gov.companieshouse.orders.api.util;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public final class StringUtils {

    public Set<String> asSet(String regexDelim, String values) {
        return Optional.ofNullable(values)
                .map(v -> Stream.of(v.split(regexDelim)))
                .get()
                .collect(Collectors.toSet());
    }
}
