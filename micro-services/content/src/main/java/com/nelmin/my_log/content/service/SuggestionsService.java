package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.service.UserService;
import com.nelmin.my_log.content.dto.ArticleDto;
import com.nelmin.my_log.content.dto.ListContentRequestDto;
import com.nelmin.my_log.content.dto.ListContentResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionsService {

    private final Article.Repo articleRepo;
    private final UserService userService;
    private final ReactionsService reactionsService;
    private final CommentService commentService;

    @Transactional
    public ListContentResponseDto suggestions(ListContentRequestDto requestDto) {
        var res = new ListContentResponseDto();

        try {
            var page = articleRepo.findAllMostPopular(PageRequest.of(requestDto.getPage(), requestDto.getMax()));

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
                                    articleDto.setCountViews(it.getCountViews());
                                    articleDto.setCountComments(commentService.countCommentsByArticleId(it.getId()));
                                    articleDto.setCountReactions(
                                            reactionsService.countReactions(it.getId())
                                                    .stream()
                                                    .map(Reaction.CountReaction::getCount)
                                                    .reduce(0L, Long::sum));
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
