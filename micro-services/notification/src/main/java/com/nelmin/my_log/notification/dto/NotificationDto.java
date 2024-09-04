package com.nelmin.my_log.notification.dto;

import com.nelmin.my_log.notification.model.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private LocalDateTime createdDate;
    private String payload;
    private Notification.Type type;
    private Boolean isRead;
}
