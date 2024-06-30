package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponseDto extends HasError {
    private Long userid;
    private Long articles;
    private Long bookmarks;
    private Long likes;
    private Long subscriptions;
    private Long subscribers;
}
