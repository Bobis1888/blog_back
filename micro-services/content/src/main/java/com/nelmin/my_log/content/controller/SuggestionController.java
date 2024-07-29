package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.content.dto.ListContentRequestDto;
import com.nelmin.my_log.content.dto.ListContentResponseDto;
import com.nelmin.my_log.content.service.SuggestionsService;
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
public class SuggestionController {

    private final SuggestionsService suggestionsService;

    @PostMapping("/suggestions")
    public ResponseEntity<ListContentResponseDto> suggestions(@RequestBody ListContentRequestDto requestDto) {
        var response = suggestionsService.suggestions(requestDto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
