package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.AuthResponseDto;
import com.nelmin.my_log.auth.dto.block.BlockedUser;
import com.nelmin.my_log.auth.dto.LoginRequestDto;
import com.nelmin.my_log.common.service.UserService;
import jakarta.servlet.ServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.nelmin.my_log.auth.service.EventsService.BLOCK_EVENT_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockService {

    @Value("${auth.max.attempts:3}")
    private Integer maxAttempts;

    @Value("${auth.global.max.attempts:15}")
    private Integer globalMaxAttempts;

    @Value("${auth.block.time:120}")
    private Long blockTime;

    @Value("${auth.global.block.time:360}")
    private Long globalBlockTime;

    private final Cache cache;
    private final ServletRequest request;
    private final EventsService eventsService;
    private final UserService userService;

    public void check(LoginRequestDto loginRequestDto, AuthResponseDto authResponse) {
        var remote = request.getRemoteAddr();

        check("global_blocked_cache_" + remote, globalMaxAttempts, globalBlockTime, authResponse);

        if (authResponse.hasErrors()) {
            log.info("Remote address {} {}", remote, authResponse.getErrors());
            return;
        }

        check("blocked_cache_" + loginRequestDto.login(), maxAttempts, blockTime, authResponse);

        if (authResponse.hasErrors() && userService.exist(loginRequestDto.login())) {
            eventsService.sendEvent(BLOCK_EVENT_NAME, Map.of(
                    "email", loginRequestDto.login(),
                    "reason", "attempts",
                    "remoteAddress", remote,
                    "time", blockTime.toString())
            );
        }
    }

    private void check(@NonNull String key, @NonNull Integer maxAttempts, @NonNull Long blockTime, @NonNull AuthResponseDto authResponse) {
        var blockedUser = Optional.ofNullable(cache.get(key, BlockedUser.class)).orElse(new BlockedUser());

        if (blockedUser.getBlockDateTime() != null) {
            var blockMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), blockedUser.getBlockDateTime().plusMinutes(blockTime));

            if (blockMinutes <= 0) {
                blockedUser = new BlockedUser();
            } else {
                authResponse.clearErrors();
                authResponse.reject("block", "login", Map.of("time", blockMinutes));
                authResponse.setSuccess(false);
                return;
            }
        }

        if (Objects.equals(blockedUser.getAttempts(), maxAttempts)) {
            blockedUser.setBlocked(true);
            blockedUser.setBlockDateTime(LocalDateTime.now());
            blockedUser.setReason("Max attempts");
            authResponse.clearErrors();
            authResponse.reject("block", "login", Map.of("time", blockTime));
        }

        blockedUser.setAttempts(blockedUser.getAttempts() + 1);
        cache.put(key, blockedUser);
    }

    public void clean(String login) {
        cache.evictIfPresent("blocked_cache_" + login);
        cache.evictIfPresent("global_blocked_cache_" + request.getRemoteAddr());
    }
}
