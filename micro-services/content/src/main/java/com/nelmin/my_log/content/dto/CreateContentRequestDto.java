package com.nelmin.my_log.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateContentRequestDto(
        Long id,
        @NotBlank(message = "nullable")
        String title,
        @NotBlank(message = "nullable")
        @Size(max = 255, message = "invalidSize")
        String preView,
        List<String> tags,
        @NotBlank(message = "nullable")
        String content) {
}
