package com.nelmin.blog.auth.controller;

import com.nelmin.blog.auth.dto.ChangeInfoRequestDto;
import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.conf.JwtTokenUtils;
import com.nelmin.blog.auth.dto.AuthResponseDto;
import com.nelmin.blog.auth.dto.LoginRequestDto;
import com.nelmin.blog.auth.dto.StateResponseDto;
import com.nelmin.blog.auth.service.AuthService;
import com.nelmin.blog.common.dto.SuccessDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserInfo userInfo;

    @PostMapping(value = "/login")
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

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-info")
    public ResponseEntity<SuccessDto> changePassword(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = authService.changeInfo(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
