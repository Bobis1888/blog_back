package com.nelmin.blog.auth.service;

import com.nelmin.blog.auth.dto.AuthResponseDto;
import com.nelmin.blog.auth.dto.RegistrationDto;
import com.nelmin.blog.auth.exceptions.InvalidUUIDException;
import com.nelmin.blog.auth.exceptions.UserNotFoundException;
import com.nelmin.blog.auth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    @Value("${server.address:http://127.0.0.1:8081}")
    private String serverAddress;

    private final User.Repo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Cache cache;

    @Transactional
    public AuthResponseDto registration(RegistrationDto registrationDto) {
        var response = new AuthResponseDto();

        if (userRepository.findUserByUsername(registrationDto.getEmail()).isPresent()) {
            response.setSuccess(false);
            response.reject("alreadyRegistered", "email");
            return response;
        }

        var uuid = UUID.randomUUID().toString();
        var user = new User();
        user.setUsername(registrationDto.getEmail());
        user.setNickName(user.getUsername().split("@")[0]);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(false);

        userRepository.save(user);

        sendEmail(user.getUsername(), uuid);

        response.setSuccess(true);
        cache.put("registration_uuid_" + uuid, user.getUsername());

        log.info("Registered User {}", user.getUsername());
        return response;
    }

    @Transactional
    public Boolean confirm(String uuid) {
        var userName = cache.get("registration_uuid_" + uuid, String.class);

        if (!StringUtils.hasText(userName)) {
            throw new InvalidUUIDException();
        }

        var user = userRepository
                .findUserByUsername(userName)
                .orElseThrow(InvalidUUIDException::new);

        // Already confirmed
        if (user.isEnabled()) {
            cache.evictIfPresent("registration_uuid_" + uuid);
            throw new InvalidUUIDException();
        }

        user.setEnabled(true);
        userRepository.save(user);
        cache.evictIfPresent("registration_uuid_" + uuid);
        return true;
    }

    public Boolean resend(String username) {
        User user = userRepository.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        var uuid = UUID.randomUUID().toString();
        cache.put("registration_uuid_" + uuid, user.getUsername());
        userRepository.save(user);
        sendEmail(user.getUsername(), uuid);
        return true;
    }

    // TODO
    private void sendEmail(String email, String uud) {
        String link = serverAddress + "/auth/confirm?uuid=" + uud;
        log.info("Link : {}", link);

        mailService.sendMail(email,
                "Подтвердите регистрацию",
                "Здравствуйте Ссылка для подтверждения регистрации : " + link);
    }
}
