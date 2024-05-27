package com.nelmin.blog.auth.service;

import com.nelmin.blog.common.conf.JwtTokenUtils;
import com.nelmin.blog.auth.dto.AuthResponseDto;
import com.nelmin.blog.auth.dto.BlockedUser;
import com.nelmin.blog.auth.dto.LoginRequestDto;
import com.nelmin.blog.auth.exceptions.UserNotFoundException;
import com.nelmin.blog.auth.model.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${auth.max.attempts:3}")
    private Long maxAttempts;

    @Value("${auth.block.time:2}")
    private Long blockTime;

    private final User.Repo userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtTokenUtils;
    private final Cache cache;

    // TODO защита от перебора
    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        var authResponse = new AuthResponseDto(false);

        checkBlock(loginRequestDto, authResponse);

        if (authResponse.hasErrors()) {
            return authResponse;
        }

        var userInfo = userRepository.findUserByUsername(loginRequestDto.login());

        var userNamePasswordToken = new UsernamePasswordAuthenticationToken(loginRequestDto.login(), loginRequestDto.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(userNamePasswordToken);
        } catch (BadCredentialsException exception) {
            countAttempts(loginRequestDto, authResponse);
            return authResponse;
        } catch (Exception exception) {
            log.error("Auth Error", exception);
            authResponse.reject("server_error", "server_error");
            return authResponse;
        }

        var user = userInfo.orElseThrow(UserNotFoundException::new);

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var token = jwtTokenUtils.generateToken(user);

        authResponse.setToken(token);
        // TODO refresh token
        authResponse.setRefreshToken(token);
        authResponse.setSuccess(true);

        cache.evictIfPresent("blocked_cache_" + loginRequestDto.login());
        log.info("User {} logged", user.getUsername());
        return authResponse;
    }

    private void countAttempts(LoginRequestDto loginRequestDto, AuthResponseDto authResponse) {
        authResponse.setSuccess(false);
        authResponse.reject("invalid", "password");
        authResponse.reject("invalid", "login");

        var blockedUser = cache.get("blocked_cache_" + loginRequestDto.login(), BlockedUser.class);

        if (blockedUser == null) {
            blockedUser = new BlockedUser();
        }

        blockedUser.setAttempts(blockedUser.getAttempts() + 1);

        if (blockedUser.getAttempts().longValue() == maxAttempts) {
            blockedUser.setBlocked(true);
            blockedUser.setBlockDateTime(LocalDateTime.now());
            blockedUser.setReason("Max attempts");
            authResponse.clearErrors();
            authResponse.reject("block", "login", Map.of("time", blockTime));
            log.info("User {} blocked {} minutes", blockedUser.getLogin(), blockTime);
        } else {
            authResponse.reject("attempts", "credentials", Map.of("value" , maxAttempts - blockedUser.getAttempts()));
        }

        cache.put("blocked_cache_" + loginRequestDto.login(), blockedUser);
    }

    private void checkBlock(LoginRequestDto loginRequestDto, AuthResponseDto authResponse) {
        var blockedUser = cache.get("blocked_cache_" + loginRequestDto.login(), BlockedUser.class);

        if (blockedUser != null && blockedUser.getBlockDateTime() != null) {
            var blockMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), blockedUser.getBlockDateTime().plusMinutes(blockTime));

            if (blockMinutes <= 0) {
                cache.evictIfPresent("blocked_cache_" + loginRequestDto.login());
            } else {
                authResponse.clearErrors();
                authResponse.reject("blocked", "login", Map.of("time", blockMinutes));
                authResponse.setSuccess(false);
            }
        }
    }
}


