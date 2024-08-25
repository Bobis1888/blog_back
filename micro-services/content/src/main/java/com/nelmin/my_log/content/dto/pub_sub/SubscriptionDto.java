package com.nelmin.my_log.content.dto.pub_sub;

import java.time.LocalDateTime;

public record SubscriptionDto(String nickname, LocalDateTime subscribedDate) {}
