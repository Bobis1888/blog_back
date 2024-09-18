package com.nelmin.my_log.subscription.dto;

import java.time.LocalDateTime;

public record SubscriptionDto(Long userId, LocalDateTime subscribedDate) {}
