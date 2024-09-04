package com.nelmin.my_log.notification.dto.kafka;

import com.nelmin.my_log.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentEvent {
    private Long userId;
    private Notification.Type type;
    private String payload;
}
