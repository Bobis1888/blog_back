package com.nelmin.my_log.auth.controller;

import com.nelmin.my_log.auth.dto.ReportRequestDto;
import com.nelmin.my_log.auth.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Secured("ROLE_USER")
    @PostMapping("/report")
    public ResponseEntity<Void> report(@Valid @RequestBody ReportRequestDto requestDto) {
        reportService.report(requestDto);

        return ResponseEntity.ok().build();
    }
}
