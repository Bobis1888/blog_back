package com.nelmin.my_log.notification;

import com.nelmin.my_log.notification.dto.ListNotificationRequestDto;
import com.nelmin.my_log.notification.dto.ListNotificationResponseDto;
import com.nelmin.my_log.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Secured("ROLE_USER")
    @PostMapping("/list")
    public ResponseEntity<ListNotificationResponseDto> list(@RequestBody ListNotificationRequestDto requestDto) {
        var res = notificationService.list(requestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(res);
    }

    @Secured("ROLE_USER")
    @PostMapping("/read/{id}")
    public ResponseEntity<Void> read(@Valid @PathVariable Long id) {
        notificationService.read(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Secured("ROLE_USER")
    @PostMapping("/read_all")
    public ResponseEntity<Void> readAll() {
        notificationService.readAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Secured("ROLE_USER")
    @GetMapping("/count_unread")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.countUnread());
    }
}
