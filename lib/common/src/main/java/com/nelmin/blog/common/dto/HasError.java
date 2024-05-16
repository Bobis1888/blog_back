package com.nelmin.blog.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class HasError {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Error> errors = new ArrayList<>();

    public Boolean hasErrors() {
        return errors != null &&!errors.isEmpty();
    }

    public void reject(Error error) {
        errors.add(error);
    }

    public void reject(String code) {
        errors.add(new Error(code));
    }

    public void reject(String code, String field) {
        errors.add(new Error(code, field));
    }

    public void reject(String code, String field, String message) {
        errors.add(new Error(code, field, message));
    }
}
