package com.nelmin.blog.content.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateContentRequestDto(
        Long id,
        @NotBlank(message = "nullable")
        String title,
        @NotBlank(message = "nullable")
        String content) {
}
