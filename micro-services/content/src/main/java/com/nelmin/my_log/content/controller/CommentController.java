package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.content.dto.comment.*;
import com.nelmin.my_log.content.service.CommentService;
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
public class CommentController {
    private final CommentService commentService;

    @Secured("ROLE_USER")
    @PostMapping("/comment/save")
    public ResponseEntity<CreateCommentResponseDto> save(@Valid @RequestBody CreateCommentRequestDto dto) {
        CreateCommentResponseDto response = commentService.save(dto);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/comment/vote")
    public ResponseEntity<Void> create(@Valid @RequestBody VoteRequestDto dto) {
        commentService.vote(dto.commentId(), dto.value());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment/list")
    public ResponseEntity<ListCommentResponseDto> list(@Valid @RequestBody ListCommentRequestDto dto) {
        var response = commentService.list(dto);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<Void> remove(@Valid @PathVariable Long id) {
         commentService.delete(id);
        return ResponseEntity.ok().build();
    }
}
