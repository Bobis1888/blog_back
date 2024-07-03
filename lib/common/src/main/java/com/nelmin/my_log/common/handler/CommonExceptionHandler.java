package com.nelmin.my_log.common.handler;

import com.nelmin.my_log.common.dto.Error;
import com.nelmin.my_log.common.dto.ExceptionResponse;
import com.nelmin.my_log.common.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CommonException.class)
    protected ResponseEntity<Object> handleExceptionInternal(CommonException exception, WebRequest webRequest) {
        log.error("Exception endpoint path : {}", ((ServletWebRequest) webRequest).getRequest().getRequestURI());
        log.error("Exception message : {}", exception.getMessage());
        ExceptionResponse response = new ExceptionResponse();
        response.setDateTime(LocalDateTime.now());
        response.setMessage(exception.getMessage());
        return new ResponseEntity<>(response, exception.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        var resultBody = new HashMap<>();
        var listError = ex.getBindingResult().getFieldErrors().stream()
                .map((err) -> new Error(err.getDefaultMessage(), err.getField())).collect(Collectors.toList());

        resultBody.put("errors", listError);

        return new ResponseEntity<>(resultBody, HttpStatus.BAD_REQUEST);
    }
}
