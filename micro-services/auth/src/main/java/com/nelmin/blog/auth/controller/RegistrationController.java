package com.nelmin.blog.auth.controller;

import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.auth.dto.RegistrationDto;
import com.nelmin.blog.auth.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping(value = "/registration")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDto registerDto) {
        var result = registrationService.registration(registerDto);

        return ResponseEntity
                .status(result.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(result);
    }

    @GetMapping(value = "/confirm")
    public ResponseEntity<Object> confirm(@RequestParam(value = "uuid") String uuid) {
        var result = registrationService.confirm(uuid);

        return ResponseEntity
                .status(result ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(new SuccessDto(result));
    }

    @PostMapping(value = "/resend_email")
    public ResponseEntity<Object> resend(@RequestBody String userName) {
        return ResponseEntity.ok(new SuccessDto(registrationService.resend(userName)));
    }
}
