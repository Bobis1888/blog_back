package com.nelmin.blog.auth.dto;

import jakarta.validation.constraints.Pattern;

public record ChangeInfoRequestDto(
        String nickname,
        @Pattern(message = "invalid", regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$")
        String password) {
}
