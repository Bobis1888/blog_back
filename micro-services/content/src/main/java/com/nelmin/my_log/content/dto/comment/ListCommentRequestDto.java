package com.nelmin.my_log.content.dto.comment;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;

public record ListCommentRequestDto(
        @NotNull(message = "nullable")
        Long contentId,
        @NotNull(message = "nullable")
        Integer page,
        @NotNull(message = "nullable")
        Integer max,
        Sort.Direction direction
) {
}
