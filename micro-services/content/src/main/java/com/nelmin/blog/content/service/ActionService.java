package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.model.User;
import com.nelmin.blog.common.service.UserService;
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
    private final SubscriptionsService subscriptionsService;
    private final UserService userService;

    public void fillInfo(ArticleDto article) {
        try {
            userRepo.getIdByNickName(article.getAuthorName()).ifPresent(it -> {
                var status = Article.Status.valueOf(article.getStatus().toUpperCase());
                var userId = it.getId();
                var actions = new Actions();
                var currentUserIsOwner = Objects.equals(userId, userInfo.getId());

                if (currentUserIsOwner) {
                    actions.setCanDelete(true);
                    actions.setCanEdit(Objects.equals(Article.Status.DRAFT, status));
                    actions.setCanPublish(Objects.equals(Article.Status.DRAFT, status));
                    actions.setCanUnpublish(List.of(Article.Status.PUBLISHED, Article.Status.PENDING).contains(status));
                } else if (status == Article.Status.PUBLISHED) {
                    var subscribed = subscriptionsService.isSubscribed(userService.resolveNickname(userId));
                    actions.setCanSubscribe(!subscribed);
                    actions.setCanUnsubscribe(subscribed);
                }

                article.setActions(actions);
            });
        } catch (Exception ex) {
            log.error("Error fill info", ex);
        }
    }
}
