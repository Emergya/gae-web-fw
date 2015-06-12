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

    @ExceptionHandler(RestException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected ResponseEntity<String> handleException(RestException ex) {
        LOG.log(Level.FINEST, "RestException handled", ex);
        return new ResponseEntity<>(ex.getMessage(), ex.getStatus());
    }

    /**
     * Exception used to return with an specific (e.g. non 200) HTTP code from a
     * <c>BaseRestWebService</c> service.
     *
     * @author lroman
     */
    public class RestException extends RuntimeException {

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
        public HttpStatus getStatus() {
            return status;
        }

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDTO processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();

        return new ValidationErrorDTO(result.getFieldErrors());
    }

    @ExceptionHandler(CustomValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDTO processCustomValidationError(CustomValidationException ex) {
        return ex.getErrors();
    }

}
