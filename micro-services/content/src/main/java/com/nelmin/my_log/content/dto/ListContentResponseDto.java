package com.nelmin.my_log.content.dto;

import com.nelmin.my_log.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListContentResponseDto extends HasError {
    private List<ArticleDto> list = new ArrayList<>();
    private Long totalRows;
    private Integer totalPages;
}
