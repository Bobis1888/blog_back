package com.nelmin.my_log.auth.service;

import com.nelmin.my_log.auth.dto.AuthResponseDto;
import com.nelmin.my_log.auth.dto.ChangePasswordRequestDto;
import com.nelmin.my_log.auth.dto.RegistrationRequestDto;
import com.nelmin.my_log.auth.dto.ResetPasswordResponse;
import com.nelmin.my_log.auth.exceptions.InvalidUUIDException;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.common.service.OAuthRegistrationService;
import com.nelmin.my_log.common.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService implements OAuthRegistrationService {

    private final Cache cache;
    private final User.Repo userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationEmailService registrationEmailService;

    @Transactional
    public AuthResponseDto registration(RegistrationRequestDto registrationRequestDto) {
        var response = new AuthResponseDto();

        if (userRepository.findUserByUsername(registrationRequestDto.email()).isPresent()) {
            response.setSuccess(false);
            response.reject("alreadyRegistered", "email");
            return response;
        }

        var uuid = UUID.randomUUID().toString();
        var user = new User();
        user.setUsername(registrationRequestDto.email());
        user.setNickName(createNickname(registrationRequestDto.email()));
        user.setPassword(passwordEncoder.encode(registrationRequestDto.password()));
        user.setEnabled(false);

        userRepository.save(user);

        registrationEmailService.sendConfirmEmail(user.getUsername(), uuid);

        response.setSuccess(true);
        cache.put("registration_uuid_" + uuid, user.getUsername());

        log.info("Registered User {}", user.getUsername());
        return response;
    }

    @Override
    public UserDetails registration(@NonNull String email) {

        if (!StringUtils.hasText(email)) {
            log.error("Invalid email {}", email);
            return null;
        }

        AtomicReference<User> user = new AtomicReference<>();

        userRepository.findUserByUsername(email).ifPresentOrElse(user::set, () -> {
            user.set(new User());
            user.get().setUsername(email);
            user.get().setNickName(createNickname(email));
            user.get().setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.get().setEnabled(true);
            userRepository.save(user.get());
            //TODO send email
        });

        return new UserInfo(user.get());
    }

    @Transactional
    public Boolean confirm(String uuid) {
        var userName = cache.get("registration_uuid_" + uuid, String.class);

        if (!StringUtils.hasText(userName)) {
            log.warn("Invalid UUID {}", uuid);
            return false;
        }

        var user = userRepository
                .findUserByUsername(userName)
                .orElseThrow(InvalidUUIDException::new);

        // Already confirmed
        if (user.isEnabled()) {
            cache.evictIfPresent("registration_uuid_" + uuid);
            log.warn("User already enabled {}", user.getId());
            return false;
        }

        userService.activateUser(user);
        cache.evictIfPresent("registration_uuid_" + uuid);
        return true;
    }

    @Transactional
    public ResetPasswordResponse resetPassword(String email) {
        var response = new ResetPasswordResponse();

        if (!StringUtils.hasText(email)) {
            response.reject("nullable", "email");
            return response;
        }

        var user = userRepository.findUserByUsername(email);

        if (user.isEmpty()) {
            response.reject("invalid", "email");
            return response;
        }

        var uuid = UUID.randomUUID().toString();
        cache.put("reset_uuid_" + uuid, user.get().getUsername());
        registrationEmailService.sendResetEmail(user.get().getUsername(), uuid);
        response.setSuccess(true);
        return response;
    }

    @Transactional
    public ResetPasswordResponse changePassword(ChangePasswordRequestDto dto) {
        var response = new ResetPasswordResponse();

        if (!StringUtils.hasText(dto.uuid())) {
            response.reject("nullable", "uuid");
            return response;
        }

        if (!StringUtils.hasText(dto.password())) {
            response.reject("nullable", "password");
            return response;
        }

        var userName = cache.get("reset_uuid_" + dto.uuid(), String.class);

        if (!StringUtils.hasText(userName)) {
            response.reject("invalid", "uuid");
            return response;
        }

        var user = userRepository.findUserByUsername(userName);

        if (user.isPresent()) {
            userService.changePassword(user.get(), dto.password());
            cache.evictIfPresent("blocked_cache_" + userName);
            cache.evictIfPresent("reset_uuid_" + dto.uuid());
            response.setSuccess(true);
        } else {
            response.reject("invalid", "uuid");
            return response;
        }

        return response;
    }

    private String createNickname(@NonNull String email) {
        var emailParts = email.split("@");

        if (emailParts.length == 2) {
            var nickname = "@" + emailParts[0];
            var id = userRepository.getIdByNickName(nickname);

            if (id.isEmpty()) {
                return nickname;
            }
        }

        return "@" + UUID.randomUUID().toString().substring(0, 16).replaceAll("-", "");
    }
}
