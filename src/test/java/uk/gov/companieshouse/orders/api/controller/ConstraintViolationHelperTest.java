package uk.gov.companieshouse.orders.api.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.util.FieldNameConverter;

@ExtendWith(MockitoExtension.class)
class ConstraintViolationHelperTest {

    @Mock
    private FieldNameConverter fieldNameConverter;

    @Mock
    private ConstraintViolation<Void> constraintViolation;

    @Mock
    private Path path;

    @InjectMocks
    private ConstraintViolationHelper constraintViolationHelper;

    @DisplayName("Should convert a single non-delimited field to snake case field")
    @Test
    void singleFieldName() {
        when(path.toString()).thenReturn("snakeCase");
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(fieldNameConverter.toSnakeCase("snakeCase")).thenReturn("snake_case");
        assertThat(constraintViolationHelper.propertyName(constraintViolation), is("snake_case"));
    }

    @DisplayName("Should convert delimited field path to snake case field")
    @Test
    void delimitedFieldPath() {
        when(path.toString()).thenReturn("ordersAny.any.snakeCase");
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(fieldNameConverter.toSnakeCase("snakeCase")).thenReturn("snake_case");
        assertThat(constraintViolationHelper.propertyName(constraintViolation), is("snake_case"));
    }

    @DisplayName("Should convert a empty field")
    @Test
    void emptyFieldName() {
        when(path.toString()).thenReturn("");
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(fieldNameConverter.toSnakeCase("")).thenReturn("");
        assertThat(constraintViolationHelper.propertyName(constraintViolation), is(""));
    }

}
