package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.model.Report;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.common.service.FillInfo;
import com.nelmin.my_log.content.dto.common.Actions;
import com.nelmin.my_log.content.dto.common.ArticleDto;
import com.nelmin.my_log.content.dto.common.ListContentResponseDto;
import com.nelmin.my_log.content.model.Article;
import lombok.NonNull;
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
    private final Report.Repo reportRepo;

    public void fillActions(@NonNull ListContentResponseDto responseDto) {
        try {
            responseDto.getList().forEach(this::getActions);
        } catch (Exception ex) {
            log.error("Error fill actions", ex);
        }
    }

    @Override
    public void fillContentInfo(@NonNull ArticleDto article) {
        try {
            userRepo.getIdByNickName(article.getAuthorName()).ifPresent(it -> {
                var userId = it.getId();
                var currentUserIsOwner = Objects.equals(userId, userInfo.getId());
                getActions(article, currentUserIsOwner);
            });
        } catch (Exception ex) {
            log.error("Error fill info", ex);
        }
    }

    private void getActions(ArticleDto article) {
        getActions(article, article.getAuthorName().equals(userInfo.getNickname()));
    }

    private void getActions(ArticleDto article, Boolean currentUserIsOwner) {
        var status = Article.Status.valueOf(article.getStatus().toUpperCase());

        var actions = new Actions();

        if (currentUserIsOwner) {
            var canEdit = (userInfo.isPremiumUser() && status != Article.Status.BLOCKED || status == Article.Status.DRAFT);

            actions.setCanDelete(status != Article.Status.BLOCKED);
            actions.setCanEdit(canEdit);
            actions.setCanPublish(Objects.equals(Article.Status.DRAFT, status));
            actions.setCanUnpublish(List.of(
                    Article.Status.PRIVATE_PUBLISHED,
                    Article.Status.PUBLISHED,
                    Article.Status.PENDING).contains(status));

        } else if (status == Article.Status.PUBLISHED) {


            if (userInfo.isAuthorized()) {
                var subscribed = subscriptionsService.isSubscribed(article.getAuthorName());
                actions.setCanSubscribe(!subscribed);
                actions.setCanUnsubscribe(subscribed);
                actions.setCanReport(!reportRepo.existsByArticleIdAndUserId(article.getId(), userInfo.getId()));
            }
        }

        actions.setCanReact(userInfo.isAuthorized());
        article.setActions(actions);
    }
}
