package uk.gov.companieshouse.orders.api.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.error.ApiErrorResponse;

public final class ApiErrorResponseEntityBuilder {
    private ApiErrorResponseEntityBuilder() {
    }

    public static Builder builder(HttpStatus httpStatus) {
        return new Builder(httpStatus);
    }

    public static final class Builder {
        private final HttpStatus httpStatus;
        private final List<ApiError> errors = new ArrayList<>();

        private Builder(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }

        public Builder addApiErrors(final List<ApiError> apiErrors) {
            this.errors.addAll(apiErrors);
            return this;
        }

        public ResponseEntity<ApiErrorResponse> build() {
            ApiErrorResponse apiResponse = new ApiErrorResponse();
            apiResponse.getErrors().addAll(errors);
            return new ResponseEntity<>(apiResponse, httpStatus);
        }
    }
}
