package uk.gov.companieshouse.orders.api.controller;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.api.error.ApiErrorResponse;
import uk.gov.companieshouse.orders.api.exception.KafkaMessagingException;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.util.FieldNameConverter;

@ControllerAdvice
public abstract class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String MESSAGE_ERROR_VALUE_KEY = "message";
    public static final String CONSTRAINT_VIOLATION_ERROR = "constraint-violation";
    private final FieldNameConverter converter;
    private final ConstraintViolationHelper constraintViolationHelper;

    public GlobalExceptionHandler(FieldNameConverter converter, ConstraintViolationHelper constraintViolationHelper) {
        this.converter = converter;
        this.constraintViolationHelper = constraintViolationHelper;
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatusCode status,
                                                                  final WebRequest request) {
        final ApiError apiError = buildBadRequestApiError(ex);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {

        if (ex.getCause() instanceof JsonProcessingException) {
            final ApiError apiError = buildBadRequestApiError((JsonProcessingException) ex.getCause());
            return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
        }

        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * Returns Http Status 500 when mongo db fails to run an operation.
     * @param ex exception
     * @return
     */
    @ExceptionHandler(MongoOperationException.class)
    public ResponseEntity<Object> handleMongoOperationException(final MongoOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

     /** Returns Http Status 500 when Kafka message sending fails
     * @param ex exception
     * @return
     */
    @ExceptionHandler(KafkaMessagingException.class)
    public ResponseEntity<Object> handleKafkaMessagingException(final KafkaMessagingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Returns bad request error for request parameter constraint violation.
     *
     * @param ex exception
     * @return response entity containing error information
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(final ConstraintViolationException ex) {
        return ApiErrorResponseEntityBuilder.builder(HttpStatus.BAD_REQUEST)
                .addApiErrors(ex.getConstraintViolations()
                        .stream()
                        .map(constraintViolation ->
                                ApiErrorBuilder.builder(CONSTRAINT_VIOLATION_ERROR, ErrorType.VALIDATION)
                                .withLocation(constraintViolationHelper.propertyName(constraintViolation))
                                .withErrorValue(MESSAGE_ERROR_VALUE_KEY, constraintViolation.getMessage())
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }

    /**
     * Utility to build ApiError from MethodArgumentNotValidException.
     *
     * @param ex the MethodArgumentNotValidException handled
     * @return the resulting ApiError
     */
    ApiError buildBadRequestApiError(final MethodArgumentNotValidException ex) {
        final List<String> errors = new ArrayList<>();

        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(converter.toSnakeCase(error.getField()) + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return new ApiError(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Utility to build ApiError from JsonProcessingException.
     *
     * @param jpe the JsonProcessingException handled
     * @return the resulting ApiError
     */
    ApiError buildBadRequestApiError(final JsonProcessingException jpe) {
        final String errorMessage = jpe.getOriginalMessage();
        return new ApiError(HttpStatus.BAD_REQUEST, singletonList(errorMessage));
    }

    protected abstract ResponseEntity<Object> handleExceptionInternal(final Exception ex,
                                                                      final Object body,
                                                                      final HttpHeaders headers,
                                                                      final HttpStatus status,
                                                                      final WebRequest request);
}