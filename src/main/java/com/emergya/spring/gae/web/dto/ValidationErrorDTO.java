package com.emergya.spring.gae.web.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.FieldError;

public final class ValidationErrorDTO implements Serializable {

    private static final long serialVersionUID = 2324324324L;

    private final Map<String, List<String>> errors;

    /**
     * Constructor receiving errors for fields.
     *
     * @param fieldErrors the errors for fields
     */
    public ValidationErrorDTO(List<FieldError> fieldErrors) {
        errors = new HashMap<>();

        for (FieldError fe : fieldErrors) {
            addFieldError(fe.getField(), fe.getDefaultMessage());
        }

    }

    /**
     * Default constructor.
     */
    public ValidationErrorDTO() {
        errors = new HashMap();
    }

    /**
     * Allows adding an error for a field.
     *
     * @param field the field id
     * @param message the message to be set
     */
    public void addFieldError(String field, String message) {

        List<String> fieldErrors;
        if (getErrors().containsKey(field)) {
            fieldErrors = getErrors().get(field);
        } else {
            fieldErrors = new ArrayList<>();
        }

        fieldErrors.add(message);
        errors.put(field, fieldErrors);
    }

    /**
     * @return the errors
     */
    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
