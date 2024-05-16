package com.nelmin.blog.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelmin.blog.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDto extends HasError {
    private Boolean success;
    private String token;
    private String refreshToken;

    public AuthResponseDto(Boolean success) {
        this.success = success;
    }
}
