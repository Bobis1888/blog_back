package com.nelmin.blog.auth.controller;

import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.auth.dto.RegistrationDto;
import com.nelmin.blog.auth.service.RegistrationService;
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

    @Value("${server.address:127.0.0.1}")
    private String serverAddress;

    private final RegistrationService registrationService;

    @PostMapping(value = "/registration")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDto registerDto) {
        var result = registrationService.registration(registerDto);

        return ResponseEntity
                .status(result.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(result);
    }

    @GetMapping(value = "/confirm")
    public ModelAndView confirm(@RequestParam(value = "uuid") String uuid, ModelMap map) {
        var result = registrationService.confirm(uuid);
        map.addAttribute("confirm-email-result", result);
        return new ModelAndView("redirect:" + resolveRedirectAddress(), map);
    }

    @PostMapping(value = "/resend_email")
    public ResponseEntity<Object> resend(@RequestBody String userName) {
        return ResponseEntity.ok(new SuccessDto(registrationService.resend(userName)));
    }

    private String resolveRedirectAddress() {
        return "http://" + serverAddress + ":4433/";
    }
}
