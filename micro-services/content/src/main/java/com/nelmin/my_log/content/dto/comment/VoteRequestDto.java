package com.nelmin.my_log.content.dto.comment;

import jakarta.validation.constraints.NotNull;

public record VoteRequestDto(
        @NotNull(message = "nullable")
        Boolean value,
        @NotNull(message = "nullable")
        Long commentId
) {
}
