package com.nelmin.my_log.statistic;

import com.nelmin.my_log.statistic.dto.StatisticsResponseDto;
import com.nelmin.my_log.statistic.service.StatisticsService;
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
    @GetMapping("/")
    public ResponseEntity<StatisticsResponseDto> statistics() {
        var response = statisticsService.getStatistics();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/list")
    public ResponseEntity<List<StatisticsResponseDto>> statisticList(@Valid @RequestBody List<Long> ids) {
        var response = statisticsService.getStatistics(ids);

        return ResponseEntity
                .status(response.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
