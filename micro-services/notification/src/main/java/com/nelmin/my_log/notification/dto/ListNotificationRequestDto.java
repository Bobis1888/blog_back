package com.nelmin.my_log.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record ListNotificationRequestDto(
        @NotNull(message = "nullable")
        @Max(value = 255, message = "invalidSize")
        Integer max,
        @NotNull(message = "nullable")
        Integer page
) {
}
