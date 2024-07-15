package com.nelmin.my_log.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelmin.my_log.common.dto.HasError;
import com.nelmin.my_log.content.model.Article;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto extends HasError {
    private Long id;
    private String title;
    private String preView;
    private String content;
    private LocalDateTime publishedDate;
    private String authorName;
    private Long likes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> tags = new ArrayList<>();
    private String status;
    private Actions actions;
    private Boolean isLiked;
    private Boolean isSaved;

    public ArticleDto(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.preView = article.getPreView();
        this.content = article.getContent();
        this.publishedDate = article.getPublishedDate();
        this.tags = article.getTags();
        this.status = article.getStatus().name();
    }


}
