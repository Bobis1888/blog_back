package com.nelmin.my_log.notification.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.notification.dto.ListNotificationRequestDto;
import com.nelmin.my_log.notification.dto.NotificationDto;
import com.nelmin.my_log.notification.dto.ListNotificationResponseDto;
import com.nelmin.my_log.notification.dto.kafka.ContentEvent;
import com.nelmin.my_log.notification.model.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Notification.Repo notificationRepo;
    private final UserInfo userInfo;

    @Transactional
    public ListNotificationResponseDto list(ListNotificationRequestDto requestDto) {
        var res = new ArrayList<NotificationDto>();
        var page = PageRequest.of(requestDto.page(), requestDto.max());
        var notifications = notificationRepo.findAllByUserId(userInfo.getId(), page);

        if (!notifications.isEmpty()) {
            notifications.stream()
                    .forEach(it -> res.add(
                                    NotificationDto
                                            .builder()
                                            .id(it.getId())
                                            .createdDate(it.getCreatedDate())
                                            .payload(it.getPayload())
                                            .type(it.getType())
                                            .isRead(it.getIsRead())
                                            .build()
                            )
                    );
        }

        return new ListNotificationResponseDto(res, notifications.getTotalPages());
    }

    @Transactional
    public void createNotification(ContentEvent event) {
        var notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(event.getType());

        if (event.getPayload() != null && !event.getPayload().isEmpty()) {
            notification.setPayload(event.getPayload());
        }

        notificationRepo.save(notification);
    }

    public void read(Long id) {
        var notification = notificationRepo.findById(id);

        if (notification.isPresent()) {
            notification.get().setIsRead(true);
            notificationRepo.save(notification.get());
        }
    }

    public void readAll() {
        notificationRepo.updateIsRead(userInfo.getId(), true);
    }

    public Long countUnread() {
        return notificationRepo.countByUserIdAndIsRead(userInfo.getId(), false);
    }
}