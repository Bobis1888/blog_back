package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private String status;
}
