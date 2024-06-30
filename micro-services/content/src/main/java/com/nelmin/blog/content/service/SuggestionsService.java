package com.nelmin.blog.content.service;

import com.nelmin.blog.common.service.UserService;
import com.nelmin.blog.content.dto.ArticleDto;
import com.nelmin.blog.content.dto.ListContentResponseDto;
import com.nelmin.blog.content.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionsService {

    private final Article.Repo articleRepo;
    private final UserService userService;

    @Transactional
    public ListContentResponseDto mostPopular() {
        var res = new ListContentResponseDto();

        try {
            var page = articleRepo.findAllMostPopular(PageRequest.of(0,10));

            if (!page.isEmpty()) {
                var userIds = page.getContent().stream().map(Article::getUserId).toList();
                Map<Long, String> names = userService.resolveNicknames(userIds);

                res.setList(
                        page.getContent()
                                .stream()
                                .map(it -> {
                                    var articleDto = new ArticleDto(it);
                                    articleDto.setContent(null);
                                    articleDto.setAuthorName(names.get(it.getUserId()));
                                    return articleDto;
                                })
                                .toList()
                );
                res.setTotalPages(page.getTotalPages());
                res.setTotalRows(page.getTotalElements());
            }
        } catch (Exception ex) {
            log.error("Error get most popular", ex);
            res.reject("internal_error", "content");
        }

        return res;
    }
}
