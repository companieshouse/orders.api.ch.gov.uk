package uk.gov.companieshouse.orders.api.util;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public final class StringHelper {

    public Set<String> asSet(String regexDelim, String values) {
        return Stream.of(Optional.ofNullable(values).orElse("").split(regexDelim))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }
}
