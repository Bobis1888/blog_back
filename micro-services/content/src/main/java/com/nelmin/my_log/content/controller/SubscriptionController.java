package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.content.dto.ListSubscriptionRequestDto;
import com.nelmin.my_log.content.dto.ListSubscriptionResponseDto;
import com.nelmin.my_log.content.service.SubscriptionsService;
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
public class SubscriptionController {

    private final SubscriptionsService subscriptionsService;

    @Secured("ROLE_USER")
    @PostMapping("/subscriptions")
    public ResponseEntity<ListSubscriptionResponseDto> subscriptions(@Valid @RequestBody ListSubscriptionRequestDto dto) {
        ListSubscriptionResponseDto response = subscriptionsService.subscriptions(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/subscribers")
    public ResponseEntity<ListSubscriptionResponseDto> subscribers(@Valid @RequestBody ListSubscriptionRequestDto dto) {
        ListSubscriptionResponseDto response = subscriptionsService.subscribers(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }


    @Secured("ROLE_USER")
    @PutMapping("/subscribe/{nickname}")
    public ResponseEntity<SuccessDto> subscribe(@Valid @PathVariable String nickname) {
        SuccessDto response = subscriptionsService.subscribe(nickname);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/unsubscribe/{nickname}")
    public ResponseEntity<SuccessDto> unsubscribe(@Valid @PathVariable String nickname) {
        SuccessDto response = subscriptionsService.unsubscribe(nickname);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
