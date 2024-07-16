package com.nelmin.my_log.content.dto;

import com.nelmin.my_log.common.dto.HasError;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponseDto extends HasError {
    private Long userid;
    private String nickname;
    private Long articles;
    private Long bookmarks;
    private Long likes;
    private Long subscriptions;
    private Long subscribers;
    @Builder.Default
    private Boolean isSubscriber = false;
    @Builder.Default
    private Boolean userIsSubscribed = false;
}
