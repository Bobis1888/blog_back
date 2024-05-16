package com.nelmin.blog.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
    private String code;
    private String field;
    private String message;

    public Error() {}

    public Error(String code) {
        this.code = code;
    }

    public Error(String code, String field) {
        this.code = code;
        this.field = field;
    }
}
