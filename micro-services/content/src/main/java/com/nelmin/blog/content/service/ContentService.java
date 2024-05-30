package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.content.dto.CreateContentRequestDto;
import com.nelmin.blog.content.dto.CreateContentResponseDto;
import com.nelmin.blog.content.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final Article.Repo articleRepo;
    private final UserInfo userInfo;

    @Transactional
    public CreateContentResponseDto save(CreateContentRequestDto dto) {
        log.info("create content: {}", dto);
        var response = new CreateContentResponseDto();
        Article article = null;

        if (dto.id() != null) {
            article = (Article) articleRepo.findById(dto.id()).orElseGet(Article::new);
        }

        if (article == null) {
            article = new Article();
        }

        article.setUserId(userInfo.getCurrentUser().getId());
        article.setContent(dto.content());
        article.setTitle(dto.title());
        articleRepo.save(article);
        response.setSuccess(true);
        return response;
    }
}
