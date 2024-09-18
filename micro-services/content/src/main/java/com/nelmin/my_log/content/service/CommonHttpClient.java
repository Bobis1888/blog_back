package com.nelmin.my_log.content.service;

import com.nelmin.my_log.user_info.jwt.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


//TODO Feign
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonHttpClient {

    private static final String PATH_PREFIX = "http://";
    private static final String TOKEN_PREFIX = "Bearer_";

    private final HttpServletRequest request;
    private final JwtTokenUtils jwtTokenUtils;
    private final RestTemplate restTemplate;

    public <T> Optional<T> exchange(String path, HttpMethod method, Class<T> clazz) {
        return exchange(path, method, null, clazz);
    }

    public <T> Optional<T> exchange(String path, HttpMethod method, Object body, Class<T> clazz) {
        AtomicReference<String> token = new AtomicReference<>();

        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).ifPresentOrElse(it -> {

            if (it.getCredentials() != null && StringUtils.hasText(it.getCredentials().toString())) {
                token.set(it.getCredentials().toString());
            }
        }, () -> token.set(jwtTokenUtils.getTokenFromRequest(request)));

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token.get());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<T> response;

        try {
            response = restTemplate.exchange(
                    PATH_PREFIX + path,
                    method,
                    new HttpEntity<>(body, headers),
                    clazz);

        } catch (Exception exception) {
            log.error("Error exchange", exception);
            return Optional.empty();
        }

        return Optional.ofNullable(response.getBody());
    }
}
