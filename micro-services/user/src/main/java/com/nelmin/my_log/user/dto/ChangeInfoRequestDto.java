package com.nelmin.my_log.user.dto;

import jakarta.validation.constraints.Pattern;

public record ChangeInfoRequestDto(
        String nickname,
        String description,
        String imagePath,
        @Pattern(message = "invalid", regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$")
        String password) {
}
