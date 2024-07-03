package com.nelmin.my_log.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void reject(String code, String field, Map<String, Object> args) {
        errors.add(new Error(code, field, args));
    }

    public void clearErrors() {
        errors.clear();
    }
}
