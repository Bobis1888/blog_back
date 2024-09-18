package com.nelmin.my_log.subscription;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.subscription.dto.ActionListResponseDto;
import com.nelmin.my_log.subscription.dto.ListRequestDto;
import com.nelmin.my_log.subscription.dto.ListResponseDto;
import com.nelmin.my_log.subscription.service.SubscriptionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionsService subscriptionsService;

    @Secured("ROLE_USER")
    @PostMapping("/list")
    public ResponseEntity<ListResponseDto> subscriptions(@Valid @RequestBody ListRequestDto dto) {
        ListResponseDto response = subscriptionsService.list(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PutMapping("/subscribe/{authorId}")
    public ResponseEntity<SuccessDto> subscribe(@Valid @PathVariable Long authorId) {
        SuccessDto response = subscriptionsService.subscribe(authorId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/unsubscribe/{authorId}")
    public ResponseEntity<SuccessDto> unsubscribe(@Valid @PathVariable Long authorId) {
        SuccessDto response = subscriptionsService.unsubscribe(authorId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @GetMapping("/actions")
    public ResponseEntity<ActionListResponseDto> actions(@Valid @RequestParam List<Long> userIds) {
        var response = subscriptionsService.actions(userIds);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ActionListResponseDto(response));
    }
}
