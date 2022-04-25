package uk.gov.companieshouse.orders.api.controller;

import java.util.HashMap;
import java.util.Map;
import uk.gov.companieshouse.api.error.ApiError;

public final class ApiErrorBuilder {
    private ApiErrorBuilder() {
    }

    public static Builder builder(final String error, final ErrorType type) {
        return new Builder(error, type);
    }

    public static final class Builder {
        private String error;
        private Map<String, String> errorValues = new HashMap<>();
        private String location;
        private String locationType;
        private ErrorType type;

        private Builder(String error, ErrorType type) {
            this.error = error;
            this.type = type;
        }

        public Builder withErrorValue(String key, String value) {
            this.errorValues.put(key, value);
            return this;
        }

        public Builder withLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder withLocationType(String locationType) {
            this.locationType = locationType;
            return this;
        }

        public ApiError build() {
            ApiError apiError = new ApiError(error, location, locationType, type.getType());
            if (! errorValues.isEmpty()) {
                apiError.setErrorValues(errorValues);
            }
            return apiError;
        }
    }
}
