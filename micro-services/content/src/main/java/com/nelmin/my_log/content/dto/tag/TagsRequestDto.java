package com.nelmin.my_log.content.dto.tag;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagsRequestDto {
    @Max(value = 1000, message = "invalidSize")
    private Integer max;
    private String query;
}
