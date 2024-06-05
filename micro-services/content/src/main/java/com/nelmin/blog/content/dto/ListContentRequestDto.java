package com.nelmin.blog.content.dto;

import jakarta.validation.constraints.NotNull;
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
    private Integer max;

    @NotNull(message = "nullable")
    private Integer page;

    private Long userId;
    private String query;
    private List<String> sortBy = new ArrayList<>();
    private Sort.Direction direction = Sort.Direction.ASC;
}
