package uk.gov.companieshouse.orders.api.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ConstraintValidationError {

    @JsonProperty("status")
    private HttpStatus status;
    @JsonProperty("constraint_errors")
    private List<String> constraintErrors;

    public HttpStatus getStatus() {
        return status;
    }
    public List<String> getConstraintErrors() {
        return constraintErrors;
    }
    public void setConstraintErrors(List<String> constraintErrors) {
        this.constraintErrors = constraintErrors;
    }
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
