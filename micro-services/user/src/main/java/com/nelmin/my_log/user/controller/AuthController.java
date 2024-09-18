package com.nelmin.my_log.user.controller;

import com.nelmin.my_log.user.dto.AuthResponseDto;
import com.nelmin.my_log.user.dto.LoginRequestDto;
import com.nelmin.my_log.user.dto.StateResponseDto;
import com.nelmin.my_log.user.service.auth.AuthService;
import com.nelmin.my_log.user_info.core.UserInfo;
import com.nelmin.my_log.user_info.jwt.JwtTokenUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserInfo userInfo;
    private final AuthService authService;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping(value = "/auth/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        var result = authService.authenticate(loginRequestDto);

        if (result.getErrors().isEmpty()) {
            HttpHeaders headers = jwtTokenUtils.createTokenHeaders(result.getToken());
            result.setToken(null);
            result.setRefreshToken(null);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(result);
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(result);
        }
    }

    @GetMapping(value = "/state")
    public ResponseEntity<StateResponseDto> state() {
        return ResponseEntity.ok(new StateResponseDto(userInfo.isAuthorized()));
    }
}
