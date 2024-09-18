package com.nelmin.my_log.user.controller;

import com.nelmin.my_log.user.dto.ResetPasswordResponse;
import com.nelmin.my_log.user.dto.RegistrationRequestDto;
import com.nelmin.my_log.user.dto.ChangePasswordRequestDto;
import com.nelmin.my_log.user.service.auth.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    @Value("${server.url:http://127.0.0.1}")
    private String serverUrl;

    private final RegistrationService registrationService;

    @PostMapping(value = "/auth/registration")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequestDto registerDto) {
        var result = registrationService.registration(registerDto);

        return ResponseEntity
                .status(result.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(result);
    }

    @GetMapping(value = "/auth/confirm")
    public ModelAndView confirm(@RequestParam(value = "uuid") String uuid, ModelMap map) {
        var result = registrationService.confirm(uuid);
        map.addAttribute("confirm-email-result", result);
        return new ModelAndView("redirect:" + resolveRedirectAddress(), map);
    }

    @GetMapping(value = "/auth/reset-password")
    public ResponseEntity<ResetPasswordResponse> resend(@RequestParam(value = "email") String email) {
        var response = registrationService.resetPassword(email);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping(value = "/auth/change-password")
    public ResponseEntity<ResetPasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequestDto dto) {
        var response = registrationService.changePassword(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    private String resolveRedirectAddress() {
        return (serverUrl.endsWith("/") ? serverUrl : serverUrl  + "/");
    }
}
