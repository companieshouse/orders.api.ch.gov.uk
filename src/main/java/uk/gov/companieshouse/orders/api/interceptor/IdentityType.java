package uk.gov.companieshouse.orders.api.interceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

enum IdentityType {
    OAUTH2("oauth2"),
    KEY("key");

    private static final Map<String, IdentityType> enumValues;

    static {
        enumValues = Arrays.stream(values())
                .collect(Collectors.toMap(IdentityType::getType, Function.identity()));
    }

    private final String type;

    IdentityType(String type) {
        this.type = type;
    }

    static IdentityType getEnumValue(String identityType) {
        return identityType != null ? enumValues.get(identityType) : null;
    }

    String getType() {
        return type;
    }

    static Set<IdentityType> asSet() {
        return Arrays.stream(IdentityType.values()).collect(Collectors.toSet());
    }
}
