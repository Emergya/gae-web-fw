package com.emergya.spring.gae.web.ws;

import com.emergya.spring.gae.web.dto.ValidationErrorDTO;

/**
 * A exception meant to be thrown when we need to communicate the client a validation error.
 *
 * Handled automatically by Controllers extending BaseRestWebService.
 *
 * @author ffarrabal
 */
public class CustomValidationException extends Exception {

    private ValidationErrorDTO errors = new ValidationErrorDTO();

    /**
     * Constructor.
     */
    public CustomValidationException() {
    }

    /**
     * Constructor that accepts a list errors.
     *
     * @param error encapsulated errors.
     */
    public CustomValidationException(final ValidationErrorDTO error) {
        this.errors = error;
    }

    /**
     * @return the errors
     */
    public final ValidationErrorDTO getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public final void setErrors(final ValidationErrorDTO errors) {
        this.errors = errors;
    }
}
