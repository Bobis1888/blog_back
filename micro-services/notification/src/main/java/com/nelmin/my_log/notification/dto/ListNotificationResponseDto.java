package com.nelmin.my_log.notification.dto;

import java.util.List;

public record ListNotificationResponseDto(
        List<NotificationDto> list,
        Integer totalPages
) {

}
