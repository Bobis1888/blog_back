package com.nelmin.my_log.notification.service;

import com.nelmin.my_log.notification.dto.ListNotificationRequestDto;
import com.nelmin.my_log.notification.dto.NotificationDto;
import com.nelmin.my_log.notification.dto.ListNotificationResponseDto;
import com.nelmin.my_log.notification.dto.kafka.ContentEvent;
import com.nelmin.my_log.notification.model.Notification;
import com.nelmin.my_log.user_info.core.UserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${notification.clean.days:15}")
    private Integer days;

    private final Notification.Repo notificationRepo;
    private final UserInfo userInfo;

    @Transactional
    public ListNotificationResponseDto list(ListNotificationRequestDto requestDto) {
        var res = new ArrayList<NotificationDto>();
        var page = PageRequest.of(requestDto.page(), requestDto.max(), Sort.by(Sort.Direction.DESC, "createdDate"));
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

    @Transactional
    public void read(Long id) {
        var notification = notificationRepo.findById(id);

        if (notification.isPresent()) {
            notification.get().setIsRead(true);
            notificationRepo.save(notification.get());
        }
    }

    @Transactional
    public void readAll() {
        notificationRepo.updateAllByUserId(userInfo.getId(), true);
    }

    public Long countUnread() {
        return notificationRepo.countByUserIdAndIsRead(userInfo.getId(), false);
    }

    @Transactional
    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.DAYS)
    public void clean() {
        log.info("Start to clean notifications");
        notificationRepo.deleteAllByCreatedDateIsBeforeAndIsRead(
                LocalDateTime.now().minusDays(days),
                true);
        log.info("End to clean notifications");
    }
}