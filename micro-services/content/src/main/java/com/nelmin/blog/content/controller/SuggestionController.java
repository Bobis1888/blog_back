package com.nelmin.blog.content.controller;

import com.nelmin.blog.content.dto.ListContentResponseDto;
import com.nelmin.blog.content.service.SuggestionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionsService suggestionsService;

    @GetMapping("/suggestions")
    public ResponseEntity<ListContentResponseDto> suggestions() {
        var response = suggestionsService.mostPopular();

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
