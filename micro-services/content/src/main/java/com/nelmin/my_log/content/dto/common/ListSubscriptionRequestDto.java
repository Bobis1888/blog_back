package com.nelmin.my_log.content.dto.common;

public record ListSubscriptionRequestDto(Type type, Integer max, Integer page) {

    public enum Type {
        SUBSCRIPTIONS, SUBSCRIBERS
    }
}
