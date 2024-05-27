package com.nelmin.blog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "nullable")
        String login,
        @NotBlank(message = "nullable")
        @Size(min = 8, max = 20, message = "invalidSize")
        String password) {
}
