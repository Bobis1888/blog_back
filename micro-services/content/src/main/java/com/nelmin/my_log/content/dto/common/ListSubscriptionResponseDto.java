package com.nelmin.my_log.content.dto.common;

import java.time.LocalDateTime;
import java.util.List;

public record ListSubscriptionResponseDto(
        List<SubscriptionDto> list,
        Integer totalRows,
        Integer totalPages) {

   public record SubscriptionDto(Long userId, LocalDateTime subscribedDate) {}
}
