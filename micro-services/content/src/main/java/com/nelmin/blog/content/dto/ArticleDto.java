package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
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
    private List<String> tags = new ArrayList<>();
    private String status;
    private Actions actions;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Actions {
        private Boolean canEdit = false;
        private Boolean canDelete = false;
        private Boolean canPublish = false;
        private Boolean canUnpublish = false;
    }
}
