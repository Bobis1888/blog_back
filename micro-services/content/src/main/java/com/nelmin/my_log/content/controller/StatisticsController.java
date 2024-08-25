package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.content.dto.common.StatisticsResponseDto;
import com.nelmin.my_log.content.service.StatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Secured("ROLE_USER")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponseDto> statistics() {
        var response = statisticsService.getStatistics();

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/statistics/list")
    public ResponseEntity<List<StatisticsResponseDto>> statisticList(@Valid @RequestBody List<Long> ids) {
        var response = statisticsService.getStatistics(ids);

        return ResponseEntity
                .status(response.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
