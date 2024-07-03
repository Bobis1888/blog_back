package com.nelmin.my_log.content.dto;

import com.nelmin.my_log.common.dto.HasError;
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
    private Long totalRows;
    private Integer totalPages;
}
