package uk.gov.companieshouse.orders.api.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.MULTI_STATUS;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.api.error.ApiErrorResponse;
import uk.gov.companieshouse.orders.api.exception.KafkaMessagingException;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.util.FieldNameConverter;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String OBJECT1 = "object1";
    private static final String OBJECT2 = "object2";
    private static final String FIELD1 = "field1";
    private static final String MESSAGE1 = "message1";
    private static final String MESSAGE2 = "message2";
    private static final String ORIGINAL_MESSAGE = "original";
    private static final HttpStatusCode ORIGINAL_STATUS = MULTI_STATUS;
    public static final String KAFKA_MESSAGING_FAILURE = "Kafka messaging failure";
    public static final String MONGO_OPERATION_FAILURE = "Mongo operation failure";

    /**
     * Extends {@link GlobalExceptionHandler} to facilitate its unit testing.
     */
    private static final class TestGlobalExceptionHandler extends GlobalExceptionHandler {

        public TestGlobalExceptionHandler(FieldNameConverter converter, ConstraintViolationHelper helper) {
            super(converter, helper);
        }

        protected ResponseEntity<Object> handleExceptionInternal(final Exception ex,
                                                                 final Object body,
                                                                 final HttpHeaders headers,
                                                                 final HttpStatus status,
                                                                 final WebRequest request) {
            return new ResponseEntity<>(body, status);
        }
    }

    @InjectMocks
    private TestGlobalExceptionHandler handlerUnderTest;

    @Mock
    private MethodArgumentNotValidException mex;

    @Mock
    private HttpMessageNotReadableException hex;

    @Mock
    private KafkaMessagingException kmex;

    @Mock
    private MongoOperationException moex;

    @Mock
    private JsonProcessingException jpe;

    @Mock
    private BindingResult result;

    @Mock
    private FieldNameConverter converter;

    private HttpHeaders headers;

    @Mock
    private WebRequest request;

    @Mock
    private ConstraintViolation<Void> constraintViolation;

    @Mock
    private ConstraintViolationHelper constraintViolationHelper;

    @Mock
    private Path path;

    @BeforeEach
    void setup() {
        headers = new HttpHeaders();
    }

    @Test
    void buildsApiErrorFromMethodArgumentNotValidException() {

        // Given
        when(mex.getBindingResult()).thenReturn(result);
        when(result.getFieldErrors()).thenReturn(Collections.singletonList(new FieldError(OBJECT1, FIELD1, MESSAGE1)));
        when(result.getGlobalErrors()).thenReturn(Collections.singletonList(new ObjectError(OBJECT2, MESSAGE2)));
        when(converter.toSnakeCase(FIELD1)).thenReturn(FIELD1);

        // When
        final ResponseEntity<Object> response = handlerUnderTest.handleMethodArgumentNotValid(mex, headers, ORIGINAL_STATUS, request);

        // Then
        final ApiError error = (ApiError) response.getBody();
        assertThat(error, is(notNullValue()));
        assertThat(error.getStatus(), is(HttpStatus.BAD_REQUEST));
        assertThat(error.getErrors().stream()
                .anyMatch(o -> o.equals(FIELD1 + ": " + MESSAGE1)), is(true));
        assertThat(error.getErrors().stream()
                .anyMatch(o -> o.equals(OBJECT2 + ": " + MESSAGE2)), is(true));
    }

    @Test
    void buildsApiErrorFromJsonProcessingException() {

        // Given
        when(hex.getCause()).thenReturn(jpe);
        when(jpe.getOriginalMessage()).thenReturn(ORIGINAL_MESSAGE);

        // When
        final ResponseEntity<Object> response =
                handlerUnderTest.handleHttpMessageNotReadable(hex, headers, ORIGINAL_STATUS, request);

        // Then
        final ApiError error = (ApiError) response.getBody();
        assertThat(error, is(notNullValue()));
        assertThat(error.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void delegatesHandlingOfNonJsonProcessingExceptionsToSpring() {

        // Given
        when(hex.getCause()).thenReturn(hex);

        // When
        final ResponseEntity<Object> response =
                handlerUnderTest.handleHttpMessageNotReadable(hex, new HttpHeaders(), ORIGINAL_STATUS, request);

        // Then
        // Note these assertions are testing behaviour implemented in the Spring framework.
        Assertions.assertEquals(response.getStatusCode(), ORIGINAL_STATUS);
        Assertions.assertTrue(response.getBody().toString().contains("Failed to read request"));
    }

    @Test
    void delegatesHandlingOfKafkaMessagingExceptionToSpring() {
        // Given
        when(kmex.getMessage()).thenReturn(KAFKA_MESSAGING_FAILURE);

        // When
        final ResponseEntity<Object> response = handlerUnderTest.handleKafkaMessagingException(kmex);

        // Then
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(response.getBody(), is(KAFKA_MESSAGING_FAILURE));
    }

    @Test
    void delegatesHandlingOfMongoOperationExceptionToSpring() {
        // Given
        when(moex.getMessage()).thenReturn(MONGO_OPERATION_FAILURE);

        // When
        final ResponseEntity<Object> response = handlerUnderTest.handleMongoOperationException(moex);

        // Then
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(response.getBody(), is(MONGO_OPERATION_FAILURE));
    }

    @DisplayName("Builds expected response entity from empty constraint violation exception")
    @Test
    void buildExpectedResponseEntity() {
        ConstraintViolationException exception = new ConstraintViolationException(Collections.emptySet());

        ResponseEntity<ApiErrorResponse> expected = ApiErrorResponseEntityBuilder.builder(HttpStatus.BAD_REQUEST).build();

        ResponseEntity<ApiErrorResponse> actual = handlerUnderTest.handleConstraintViolation(exception);

        assertThat(actual.getStatusCode(), is(expected.getStatusCode()));
    }

    @DisplayName("Builds expected response entity from parameter constraint violation exception")
    @Test
    void buildExpectedResponseEntityFromParameterConstrainViolation() {

        Set<ConstraintViolation<Void>> constraintViolations = new HashSet<ConstraintViolation<Void>>();
        constraintViolations.add(constraintViolation);
        when(constraintViolation.getMessage()).thenReturn("error message");
        when(constraintViolationHelper.propertyName(constraintViolation)).thenReturn("converted_field");

        ConstraintViolationException exception = new ConstraintViolationException(constraintViolations);

        ResponseEntity<ApiErrorResponse> expected = ApiErrorResponseEntityBuilder.builder(HttpStatus.BAD_REQUEST)
                .addApiErrors(Collections.singletonList(
                        ApiErrorBuilder.builder("field-error", ErrorType.VALIDATION)
                        .withErrorValue("message", "error message")
                        .withLocation("converted_field")
                                .build()))

                .build();

        ResponseEntity<ApiErrorResponse> actual = handlerUnderTest.handleConstraintViolation(exception);

        assertThat(actual.getStatusCode(), is(expected.getStatusCode()));
    }
}
