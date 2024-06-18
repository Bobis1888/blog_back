package com.nelmin.blog.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePreviewRequestDto(
        @NotNull(message = "nullable")
        @Size(max = 255, message = "invalidSize")
        String content
) {
}
