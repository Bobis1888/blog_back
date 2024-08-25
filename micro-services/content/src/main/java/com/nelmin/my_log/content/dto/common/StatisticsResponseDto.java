package com.nelmin.my_log.content.dto.common;

import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.content.model.UserStatistic;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponseDto extends HasError {
    private Long userid;
    private String nickname;
    private Long views;
    private Long articles;
    private Long reactions;
    private Long subscribers;
    private Long comments;
    private Long rating;

    public void setStatistics(UserStatistic userStatistic) {
        this.userid = userStatistic.getUserId();
        this.nickname = userStatistic.getNickname();
        this.views = userStatistic.getViews();
        this.articles = userStatistic.getArticles();
        this.reactions = userStatistic.getReactions();
        this.subscribers = userStatistic.getSubscribers();
        this.comments = userStatistic.getComments();
    }
}
