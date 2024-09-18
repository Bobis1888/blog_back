package com.nelmin.my_log.user.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRequestDto(
        @NotBlank(message = "nullable")
        @Size(max = 255, message = "invalidSize")
        String type,
        @Nonnull
        Long articleId,
        @Size(max = 255, message = "invalidSize")
        String description) {
}
