package com.nelmin.my_log.auth.dto;

import jakarta.validation.constraints.Pattern;

public record ChangeInfoRequestDto(
        String nickname,
        String description,
        @Pattern(message = "invalid", regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$")
        String password) {
}
