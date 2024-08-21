package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.service.FillContentInfo;
import com.nelmin.my_log.common.service.FillStatisticInfo;
import com.nelmin.my_log.common.service.UserService;
import com.nelmin.my_log.content.Utils;
import com.nelmin.my_log.content.dto.ArticleDto;
import com.nelmin.my_log.content.dto.BookmarksRequestDto;
import com.nelmin.my_log.content.dto.BookmarksResponseDto;
import com.nelmin.my_log.content.dto.StatisticsResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.content.model.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarksService implements FillContentInfo<ArticleDto>, FillStatisticInfo<StatisticsResponseDto> {
    private final UserInfo userInfo;
    private final Bookmark.Repo bookmarkRepository;
    private final Article.Repo articleRepository;
    private final UserService userService;

    @Transactional
    public SuccessDto addToBookmarks(Long articleId) {
        var response = new SuccessDto(false);

        try {
            var userId = userInfo.getId();
            var bookmark = bookmarkRepository.findByArticleIdAndUserId(articleId, userId).orElse(new Bookmark());

            if (bookmark.getId() != null) {
                response.reject("alreadyAdded", "article");
            } else {
                bookmark.setArticleId(articleId);
                bookmark.setUserId(userId);
                bookmarkRepository.save(bookmark);
                response.setSuccess(bookmark.getId() != null);
            }
        } catch (Exception ex) {
            log.error("Error save to bookmark", ex);
        }

        return response;
    }

    @Transactional
    public SuccessDto removeFromBookmarks(Long articleId) {
        var response = new SuccessDto(false);
        var userId = userInfo.getId();
        var bookmark = bookmarkRepository.findByArticleIdAndUserId(articleId, userId);

        if (bookmark.isEmpty()) {
            response.reject("notFound", "bookmark");
        } else {
            bookmarkRepository.deleteById(bookmark.get().getId());
            response.setSuccess(true);
        }

        return response;
    }

    @Override
    @Transactional
    public void fillContentInfo(ArticleDto response) {
        var exists = bookmarkRepository.existsByArticleIdAndUserId(response.getId(), userInfo.getId());
        response.setIsSaved(exists);
    }

    @Override
    @Transactional
    public void fillStatisticInfo(StatisticsResponseDto response) {
        response.setBookmarks(bookmarkRepository.countByUserId(response.getUserid()));
    }

    @Transactional
    public BookmarksResponseDto list(BookmarksRequestDto requestDto) {
        var response = new BookmarksResponseDto();
        String[] sortBy = null;

        if (requestDto.getSortBy() != null &&
                !requestDto.getSortBy().isEmpty()) {
            sortBy = Utils.getSortProperties(requestDto.getSortBy(), Article.class);
        }

        if (sortBy == null || sortBy.length == 0) {
            sortBy = new String[]{"id"};
        }

        var pageRequest = PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        sortBy
                )
        );

        var dbResponse = articleRepository.findAllInBookmarks(userInfo.getId(), pageRequest);

        if (dbResponse.isEmpty()) {
            response.setList(new ArrayList<>());
        } else {
            response.setList(dbResponse
                    .getContent()
                    .stream()
                    .map((it) -> {
                        var res = new ArticleDto(it);
                        res.setIsSaved(true);
                        res.setAuthorName(userService.resolveNickname(it.getUserId()));
                        res.setCountViews(it.getCountViews());
                        fillContentInfo(res);
                        return res;
                    })
                    .toList());
            response.setTotalRows(dbResponse.getTotalElements());
            response.setTotalPages(dbResponse.getTotalPages());
        }

        return response;
    }
}
