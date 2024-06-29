package com.nelmin.blog.content.controller;

import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.content.service.LikeService;
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
public class LikeController {

    private final LikeService likeService;

    @Secured("ROLE_USER")
    @PutMapping("/like/{id}")
    public ResponseEntity<SuccessDto> like(@Valid @PathVariable Long id) {
        var response = likeService.like(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/like/{id}")
    public ResponseEntity<SuccessDto> dislike(@Valid @PathVariable Long id) {
        var response = likeService.dislike(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
