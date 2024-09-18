package com.nelmin.my_log.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "nullable")
        String login,
        @NotBlank(message = "nullable")
        String password) {
}
