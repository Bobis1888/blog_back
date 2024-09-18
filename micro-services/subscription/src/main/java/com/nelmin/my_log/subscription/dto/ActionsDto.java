package com.nelmin.my_log.subscription.dto;

public record ActionsDto(
        Long userId,
        Boolean canSubscribe,
        Boolean canUnsubscribe) {
}
