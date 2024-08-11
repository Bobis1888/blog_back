package com.nelmin.my_log.content.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequestDto(
        @Size(max = 256, message = "invalidSize")
        @NotBlank(message = "nullable")
        String comment,
        @NotNull(message = "nullable")
        Long contentId
) {
}
