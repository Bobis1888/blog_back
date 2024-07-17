package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.content.service.ReactionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReactionsController {

    private final ReactionsService reactionsService;

    @Secured("ROLE_USER")
    @PutMapping("/react/{id}/{type}")
    public ResponseEntity<SuccessDto> react(@Valid @PathVariable Long id, @Valid @PathVariable String type) {
        var response = reactionsService.react(id, type);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/react/{id}")
    public ResponseEntity<SuccessDto> remove(@Valid @PathVariable Long id) {
        var response = reactionsService.remove(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
