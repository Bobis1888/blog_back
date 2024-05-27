package com.nelmin.blog.auth.dto;

import com.nelmin.blog.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordResponse extends HasError {
    private Boolean success;
}
