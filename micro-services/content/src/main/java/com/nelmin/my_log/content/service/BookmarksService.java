package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.content.dto.common.ArticleDto;
import com.nelmin.my_log.content.model.Bookmark;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarksService implements FillInfo<ArticleDto> {
    private final UserInfo userInfo;
    private final Bookmark.Repo bookmarkRepository;

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
}
