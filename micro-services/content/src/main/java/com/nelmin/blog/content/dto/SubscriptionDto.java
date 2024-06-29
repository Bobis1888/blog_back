package com.nelmin.blog.content.dto;

import java.time.LocalDateTime;

public record SubscriptionDto(String nickname, LocalDateTime subscribedDate) {}
