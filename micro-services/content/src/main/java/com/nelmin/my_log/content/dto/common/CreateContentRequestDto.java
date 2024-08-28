package com.nelmin.my_log.content.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateContentRequestDto(
        Long id,
        @NotBlank(message = "nullable")
        @Size(max = 255, message = "invalidSize")
        String title,
        List<String> tags,
        @NotBlank(message = "nullable")
        String content) {
}
