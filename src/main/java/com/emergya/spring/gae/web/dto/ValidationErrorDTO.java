package com.emergya.spring.gae.web.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.FieldError;

public final class ValidationErrorDTO {

    private final Map<String, List<String>> errors;

    public ValidationErrorDTO(List<FieldError> fieldErrors) {
        errors = new HashMap<>();

        for (FieldError fe : fieldErrors) {
            addFieldError(fe.getField(), fe.getDefaultMessage());
        }

    }

    public ValidationErrorDTO() {
        errors = new HashMap();
    }

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
