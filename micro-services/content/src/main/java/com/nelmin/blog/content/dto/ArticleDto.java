package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
import com.nelmin.blog.content.model.Article;
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
    private List<String> tags = new ArrayList<>();
    private String status;
    private Actions actions;
    private Boolean isLiked = false;
    private Boolean isSaved = false;

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
