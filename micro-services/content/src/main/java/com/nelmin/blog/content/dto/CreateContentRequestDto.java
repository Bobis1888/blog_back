package com.nelmin.blog.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContentRequestDto(
        Long id,
        @NotBlank(message = "nullable")
        String title,
        @NotBlank(message = "nullable")
        @Size(max = 255, message = "invalidSize")
        String preView,
        @NotBlank(message = "nullable")
        String content) {
}
