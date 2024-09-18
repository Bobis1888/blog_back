package com.nelmin.my_log.content.dto.action;

import java.util.List;

public record SubscriptionActions(List<Action> list) {

    public record Action(
            Long userId,
            Boolean canSubscribe,
            Boolean canUnsubscribe
    ) {
    }
}
