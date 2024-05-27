package com.nelmin.blog.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequestDto(
        @NotBlank(message = "nullable")
        @Email(message = "invalid", flags = {Pattern.Flag.CASE_INSENSITIVE})
        String email,
        @NotBlank(message = "nullable")
        @Size(min = 8, max = 20, message = "invalidSize")
        @Pattern(message = "invalid", regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$")
        String password) {
}
