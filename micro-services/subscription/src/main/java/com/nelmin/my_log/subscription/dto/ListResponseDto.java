package com.nelmin.my_log.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListResponseDto {
    private List<SubscriptionDto> list;
    private Long totalRows;
    private Integer totalPages;
}
