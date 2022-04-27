package uk.gov.companieshouse.orders.api.interceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum IdentityType {
    OAUTH2("oauth2"),
    KEY("key"),
    UNKNOWN("unknown");

    private static final Map<String, IdentityType> enumValues;

    static {
        enumValues = Arrays.stream(values())
                .collect(Collectors.toMap(IdentityType::getType, Function.identity()));
    }
    private final String type;


    IdentityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static IdentityType getEnumValue(String companyStatus) {
        return companyStatus != null ? enumValues.get(companyStatus) : UNKNOWN;
    }
}
