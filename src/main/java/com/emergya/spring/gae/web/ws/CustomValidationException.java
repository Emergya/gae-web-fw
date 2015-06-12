package com.emergya.spring.gae.web.ws;

import com.emergya.spring.gae.web.dto.ValidationErrorDTO;

/**
 *
 * @author ffarrabal
 */
public class CustomValidationException extends Exception {

    private ValidationErrorDTO errors = new ValidationErrorDTO();

    public CustomValidationException() {
    }

    //Constructor that accepts a list errors
    public CustomValidationException(ValidationErrorDTO error) {
        this.errors = error;
    }

    /**
     * @return the errors
     */
    public ValidationErrorDTO getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(ValidationErrorDTO errors) {
        this.errors = errors;
    }
}
