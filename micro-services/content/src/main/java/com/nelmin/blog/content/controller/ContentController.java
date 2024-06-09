package com.nelmin.blog.content.controller;

import com.nelmin.blog.content.dto.*;
import com.nelmin.blog.content.model.Article;
import com.nelmin.blog.content.service.ContentService;
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
public class ContentController {

    private final ContentService contentService;

    // TODO split create/edit
    @Secured("ROLE_USER")
    @PostMapping("/save")
    public ResponseEntity<CreateContentResponseDto> save(@Valid @RequestBody CreateContentRequestDto dto) {
        CreateContentResponseDto response = contentService.save(dto);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DeleteContentResponseDto> delete(@Valid @PathVariable Long id) {
        DeleteContentResponseDto response = contentService.delete(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ArticleDto> view(@Valid @PathVariable Long id) {
        ArticleDto response = contentService.get(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Deprecated
    @Secured("ROLE_USER")
    @GetMapping("/publish/{id}")
    public ResponseEntity<PublishContentResponseDto> publish(@Valid @PathVariable Long id) {
        PublishContentResponseDto response = contentService.changeStatus(id, Article.Status.PUBLISHED);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Deprecated
    @Secured("ROLE_USER")
    @GetMapping("/unpublish/{id}")
    public ResponseEntity<PublishContentResponseDto> unpublish(@Valid @PathVariable Long id) {
        PublishContentResponseDto response = contentService.changeStatus(id, Article.Status.DRAFT);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/list")
    public ResponseEntity<ListContentResponseDto> list(@Valid @RequestBody ListContentRequestDto dto) {
        ListContentResponseDto response = contentService.list(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @GetMapping("/change-status/{id}")
    public ResponseEntity<PublishContentResponseDto> changeStatus(@Valid @PathVariable Long id, @RequestBody Article.Status status) {
        PublishContentResponseDto response = contentService.changeStatus(id, status);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/tags")
    public ResponseEntity<TagsResponseDto> tags(@Valid @RequestBody TagsRequestDto dto) {
        var response = contentService.tags(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
