package com.nelmin.my_log.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequestDto(
        @NotBlank(message = "nullable")
        @Email(message = "invalid", flags = {Pattern.Flag.CASE_INSENSITIVE})
        String email,
        @NotBlank(message = "nullable")
        @Size(min = 8, max = 100, message = "invalidSize")
        @Pattern(message = "invalid", regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$")
        String password) {
}
