package uk.gov.companieshouse.orders.api.controller;

import java.util.Arrays;
import java.util.Optional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.util.FieldNameConverter;

@Component
public class ConstraintViolationHelper {
    private final FieldNameConverter converter;

    public ConstraintViolationHelper(FieldNameConverter converter) {
        this.converter = converter;
    }

    String propertyName(ConstraintViolation<?> constraintViolation) {
        return Optional.ofNullable(constraintViolation)
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .flatMap(path -> Arrays.stream(path.split("\\."))
                        .reduce((first, last) -> last))
                .map(converter::toSnakeCase)
                .orElse("");
    }
}
