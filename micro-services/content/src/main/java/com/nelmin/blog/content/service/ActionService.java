package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.model.User;
import com.nelmin.blog.content.dto.Actions;
import com.nelmin.blog.content.dto.ArticleDto;
import com.nelmin.blog.content.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService implements FillInfo<ArticleDto> {
    private final UserInfo userInfo;
    private final User.Repo userRepo;

    public void fillInfo(ArticleDto article) {
        var actions = calculate(userRepo.getIdByNickName(article.getAuthorName()).get().getId(),
                Article.Status.valueOf(article.getStatus()));
        article.setActions(actions);
    }

    private Actions calculate(Long userId, Article.Status status) {
        var actions = new Actions();
        var currentUserIsOwner = Objects.equals(userId, userInfo.getCurrentUser().getId());

        if (currentUserIsOwner) {
            actions.setCanDelete(true);
            actions.setCanEdit(Objects.equals(Article.Status.DRAFT, status));
            actions.setCanPublish(Objects.equals(Article.Status.DRAFT, status));
            actions.setCanUnpublish(List.of(Article.Status.PUBLISHED, Article.Status.PENDING).contains(status));
        }

        return actions;
    }
}
