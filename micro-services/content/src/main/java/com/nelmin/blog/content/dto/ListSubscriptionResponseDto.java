package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListSubscriptionResponseDto extends HasError {
    private List<SubscriptionDto> list;
    private Integer totalPages;
}
