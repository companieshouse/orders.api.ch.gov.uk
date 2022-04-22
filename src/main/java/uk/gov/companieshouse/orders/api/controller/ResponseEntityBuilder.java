package uk.gov.companieshouse.orders.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;

public final class ResponseEntityBuilder {
    public static <T> Builder<T> builder(HttpStatus httpStatus) {
        return new Builder<>(httpStatus);
    }

    private ResponseEntityBuilder() {
    }

    public static final class Builder<T> {
        private final HttpStatus httpStatus;
        private Map<String, Object> headers;
        private T data;
        private List<ApiError> errors = new ArrayList<>();

        private Builder(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }

        public Builder<T> withHeaders(Map<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public Builder<T> withData(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> addApiError(ApiError apiError) {
            this.errors.add(apiError);
            return this;
        }

        public Builder<T> addApiErrors(List<ApiError> apiErrors) {
            this.errors.addAll(apiErrors);
            return this;
        }

        public ResponseEntity<Object> build() {
            ApiResponse<T> apiResponse = new ApiResponse<>(httpStatus.value(), headers, data);
            apiResponse.getErrors().addAll(errors);
            return new ResponseEntity<>(apiResponse, httpStatus);
        }
    }
}
