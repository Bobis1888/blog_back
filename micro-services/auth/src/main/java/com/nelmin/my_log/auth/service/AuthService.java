package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.ChangeInfoRequestDto;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.conf.JwtTokenUtils;
import com.nelmin.my_log.auth.dto.AuthResponseDto;
import com.nelmin.my_log.auth.dto.BlockedUser;
import com.nelmin.my_log.auth.dto.LoginRequestDto;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.exception.UserNotFoundException;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.common.service.UserService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final UserInfo userInfo;

    // TODO
    @Transactional
    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        var authResponse = new AuthResponseDto(false);

        checkBlock(loginRequestDto, authResponse);

        if (authResponse.hasErrors()) {
            return authResponse;
        }

        UserDetails userInfo;

        try {
            userInfo = userDetailsService.loadUserByUsername(loginRequestDto.login());
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequestDto.login(), loginRequestDto.password());
            authentication = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException | UsernameNotFoundException | UserNotFoundException exception) {
            countAttempts(loginRequestDto, authResponse);
            return authResponse;
        } catch (Exception exception) {
            log.error("Auth Error", exception);
            authResponse.reject("serverError");
            return authResponse;
        }

        userService.updateLastLoginDate(userInfo);
        var token = jwtTokenUtils.generateToken(userInfo);
        authResponse.setToken(token);
        // TODO refresh token
        authResponse.setRefreshToken(token);
        authResponse.setSuccess(true);

        cache.evictIfPresent("blocked_cache_" + loginRequestDto.login());
        log.info("User {} logged", userInfo.getUsername());
        return authResponse;
    }

    @Transactional
    public SuccessDto changeNickname(@NonNull ChangeInfoRequestDto dto) {
        var res = new SuccessDto(false);

        if (!StringUtils.hasText(dto.nickname())) {
            res.reject("invalid", "nickname");
            return res;
        }

        try {
            User user = (User) userInfo.getCurrentUser();

            if (StringUtils.hasText(dto.nickname())) {

                // TODO
                if (userRepository.getIdByNickName(dto.nickname()).isPresent()) {
                    res.reject("invalid", "nickname");
                } else {
                    userService.changeNickname(user, dto.nickname());
                }

                res.setSuccess(!res.hasErrors());
            }
        } catch (Exception ex) {
            log.error("Error change nickname", ex);
            res.setSuccess(false);
        }

        return res;
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
            authResponse.reject("attempts", "credentials", Map.of("value", maxAttempts - blockedUser.getAttempts()));
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
                authResponse.reject("block", "login", Map.of("time", blockMinutes));
                authResponse.setSuccess(false);
            }
        }
    }

    public SuccessDto changeDescription(ChangeInfoRequestDto dto) {
        var res = new SuccessDto(false);

        if (!StringUtils.hasText(dto.description())) {
            res.reject("nullable", "description");
        } else {
            try {
                User user = (User) userInfo.getCurrentUser();
                userService.changeDescription(user, dto.description());
                res.setSuccess(true);
            } catch (Exception ex) {
                log.error("Error change description", ex);
                res.setSuccess(false);
            }
        }

        return res;
    }
}


