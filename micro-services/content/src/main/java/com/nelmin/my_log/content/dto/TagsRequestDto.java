package com.nelmin.my_log.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagsRequestDto {

    @NotNull(message = "nullable")
    @Max(value = 255, message = "invalidSize")
    private Integer max;

    @NotNull(message = "nullable")
    private Integer page;
    private String query;
}
