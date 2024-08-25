package com.nelmin.my_log.content.dto.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.nelmin.my_log.content.model.specification.ContentSpecificationFactory;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListContentRequestDto {

    @NotNull(message = "nullable")
    private ContentSpecificationFactory.RequestType type;

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
        private List<Long> exclude = new ArrayList<>();

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}
