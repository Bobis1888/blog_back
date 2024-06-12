package com.nelmin.blog.content.dto;

import com.nelmin.blog.common.dto.HasError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShortArticleDto extends HasError {
    private Long id;
    private String title;
    private String preView;
    private LocalDateTime publishedDate;
    private String authorName;
    private List<String> tags = new ArrayList<>();
    private ArticleDto.Actions actions;
}
