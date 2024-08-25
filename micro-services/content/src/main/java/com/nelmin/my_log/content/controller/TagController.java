package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.content.dto.tag.TagsRequestDto;
import com.nelmin.my_log.content.dto.tag.TagsResponseDto;
import com.nelmin.my_log.content.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/tag/list")
    public ResponseEntity<TagsResponseDto> list(@Valid @RequestBody TagsRequestDto dto) {
        var response = tagService.list(dto.getMax(), dto.getQuery());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new TagsResponseDto(response));
    }

    @GetMapping("/tag/suggestions")
    public ResponseEntity<TagsResponseDto> suggestions() {
        //TODO suggestion
        var response = tagService.list(10, "");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new TagsResponseDto(response));
    }
}
