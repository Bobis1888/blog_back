package com.nelmin.my_log.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @Size(min = 36, max = 36, message = "invalidSize")
        @NotBlank(message = "nullable")
        String uuid,
        @NotBlank(message = "nullable")
        @Size(min = 8, max = 20, message = "invalidSize")
        @Pattern(message = "invalid", regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$")
        String password) {
}
