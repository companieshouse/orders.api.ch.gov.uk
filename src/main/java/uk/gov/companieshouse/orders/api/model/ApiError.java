package uk.gov.companieshouse.orders.api.model;

import java.util.Objects;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ApiError {
    private final HttpStatus status;
    private List<String> errors = new ArrayList<>();

    public ApiError(final HttpStatus status, final List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public ApiError(final HttpStatus status, final String error) {
        this.status = status;
        this.errors.add(error);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiError apiError = (ApiError) o;
        return status == apiError.status && Objects.equals(errors, apiError.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, errors);
    }
}
