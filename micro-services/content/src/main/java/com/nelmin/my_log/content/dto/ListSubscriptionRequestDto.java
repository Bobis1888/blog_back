package com.nelmin.my_log.content.dto;

import jakarta.validation.constraints.Max;
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
public class ListSubscriptionRequestDto {

    @NotNull(message = "nullable")
    @Max(value = 255, message = "invalidSize")
    private Integer max;

    @NotNull(message = "nullable")
    private Integer page;

    private List<String> sortBy = new ArrayList<>();

    private Sort.Direction direction = Sort.Direction.DESC;

    private String query;
}
