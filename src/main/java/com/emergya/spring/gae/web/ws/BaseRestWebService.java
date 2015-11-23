package com.emergya.spring.gae.web.ws;

import com.emergya.spring.gae.web.dto.ValidationErrorDTO;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base class for RestControllers.
 *
 * Its use as base class is required for the automatic handling of
 * <c>RestException</c>
 * to ease the return of HTTP error codes.
 *
 * @author lroman
 */
@RestController
public abstract class BaseRestWebService {

    private static final Logger LOG = Logger.getLogger(BaseRestWebService.class.getName());

    /**
     * Handles an RestException returning an standard response.
     *
     * This allows returning response codes other than 200 OK easily from controller methods.
     *
     * @param ex the exception to be handled.
     * @return The response to be shown containing the message and status error.
     */
    @ExceptionHandler(RestException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected final ResponseEntity<String> handleException(RestException ex) {
        LOG.log(Level.FINEST, "RestException handled", ex);
        return new ResponseEntity<>(ex.getMessage(), ex.getStatus());
    }

    /**
     * Exception used to return with an specific (e.g. non 200) HTTP code from a
     * <c>BaseRestWebService</c> service.
     *
     * @author lroman
     */
    public static class RestException extends RuntimeException {

        private final HttpStatus status;

        /**
         * Builds a new RestException instance.
         *
         * @param status The HttpStatus enumeration value corresponding to the code that the response should have.
         * @param message The message or code to be sent to the client.
         * @param innerException The exception causing this one to be thrown, if any.
         */
        public RestException(HttpStatus status, String message, Throwable innerException) {
            super(message, innerException);
            this.status = status;
        }

        /**
         * @return the status
         */
        public final HttpStatus getStatus() {
            return status;
        }

    }

    /**
     * Handles MethodArgumentNotValidException so we return an encapsulated validation error object automatically instead just fail.
     *
     * @param ex the handled exception, containin automatic validation error results
     * @return the encapsulated error object that
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final ValidationErrorDTO processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();

        return new ValidationErrorDTO(result.getFieldErrors());
    }

    /**
     * Handles CustomValidationException so we can manually trigger validation error responses.
     *
     * @param ex the custom validation exception containing manual validation errors
     * @return the encapsulated error object
     */
    @ExceptionHandler(CustomValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final ValidationErrorDTO processCustomValidationError(CustomValidationException ex) {
        return ex.getErrors();
    }

}
