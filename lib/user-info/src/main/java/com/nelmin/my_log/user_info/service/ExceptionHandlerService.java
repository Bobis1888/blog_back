package com.nelmin.my_log.user_info.service;

import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.user_info.dto.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerService {

    private final UserInfo userInfo;

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleExceptionInternal(AccessDeniedException exception, WebRequest webRequest) {
        log(((ServletWebRequest) webRequest).getRequest().getRequestURI(), exception.getMessage());
        ExceptionResponse response = new ExceptionResponse();
        response.setDateTime(LocalDateTime.now());
        response.setMessage(exception.getMessage());

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (userInfo.isAuthorized()) {
            status = HttpStatus.FORBIDDEN;
        }

        return new ResponseEntity<>(response, status);
    }

    private void log(String path, String message) {
        log.error("Exception endpoint path : {}", path);
        log.error("Exception message : {}", message);
    }
}
