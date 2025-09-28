package com.earmitage.core.security.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class GenericResponse {

    private String message;
    private String error;
    private boolean success;

    public GenericResponse(final String message, final boolean success) {
        super();
        this.message = message;
        this.success = success;
    }

    public GenericResponse(final List<ObjectError> allErrors, final String error) {
        this.error = error;
        final String temp = allErrors.stream().map(e -> {
            if (e instanceof FieldError) {
                return "{\"field\":\"" + ((FieldError) e).getField() + "\",\"defaultMessage\":\""
                        + e.getDefaultMessage() + "\"}";
            } else {
                return "{\"object\":\"" + e.getObjectName() + "\",\"defaultMessage\":\"" + e.getDefaultMessage()
                        + "\"}";
            }
        }).collect(Collectors.joining(","));
        message = "[" + temp + "]";
    }
}
