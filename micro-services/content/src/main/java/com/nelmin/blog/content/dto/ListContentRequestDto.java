package com.nelmin.blog.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListContentRequestDto {

    @NotNull(message = "nullable")
    @Max(value = 255, message = "invalidSize")
    private Integer max;

    @NotNull(message = "nullable")
    private Integer page;

    private List<String> sortBy = new ArrayList<>();
    private Sort.Direction direction = Sort.Direction.DESC;

    private Search search;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Search {
        private String query;
        private List<String> tags = new ArrayList<>();
        private String author;
    }
}
