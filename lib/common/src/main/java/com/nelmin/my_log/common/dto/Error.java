package com.nelmin.my_log.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
    private String code;
    private String field;
    private Map<String, Object> args;

    public Error() {}

    public Error(String code) {
        this.code = code;
    }

    public Error(String code, String field) {
        this.code = code;
        this.field = field;
    }

    public Error(String code, String field, Map<String, Object> args) {
        this.code = code;
        this.field = field;
        this.args = args;
    }
}
