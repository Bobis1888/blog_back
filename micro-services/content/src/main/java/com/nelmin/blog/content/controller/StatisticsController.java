package com.nelmin.blog.content.controller;

import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.content.dto.StatisticsResponseDto;
import com.nelmin.blog.content.service.LikeService;
import com.nelmin.blog.content.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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

    // TODO stat by user nickname
}
