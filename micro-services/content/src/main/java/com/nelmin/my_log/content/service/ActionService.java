package com.nelmin.my_log.content.service;

import com.nelmin.my_log.content.dto.action.Actions;
import com.nelmin.my_log.content.dto.action.SubscriptionActions;
import com.nelmin.my_log.content.dto.common.ArticleDto;
import com.nelmin.my_log.content.dto.common.ListContentResponseDto;
import com.nelmin.my_log.content.model.Article;
import com.nelmin.my_log.user_info.core.UserInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService implements FillInfo<ArticleDto> {

    private final UserInfo userInfo;
    private final CommonHttpClient httpClient;

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
            var currentUserIsOwner = Objects.equals(article.getAuthorId(), userInfo.getId());
            getActions(article, currentUserIsOwner, true);
        } catch (Exception ex) {
            log.error("Error fill info", ex);
        }
    }

    private void getActions(ArticleDto article) {
        getActions(article, article.getAuthorId().equals(userInfo.getId()), false);
    }

    private void getActions(ArticleDto article, Boolean currentUserIsOwner, Boolean deep) {
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

        } else if (List.of(Article.Status.PUBLISHED, Article.Status.PRIVATE_PUBLISHED).contains(status)) {

            if (userInfo.isAuthorized() && deep) {
                var response = httpClient.exchange("subscription/actions?userIds=" + article.getAuthorId(), HttpMethod.GET, SubscriptionActions.class);

                response.ifPresent(it -> {
                    var action = it.list().isEmpty() ? null : it.list().get(0);

                    if (action != null) {
                        actions.setCanSubscribe(action.canSubscribe());
                        actions.setCanUnsubscribe(action.canUnsubscribe());
                    }
                });

                actions.setCanReport(true);
            }
        }

        actions.setCanReact(userInfo.isAuthorized());
        article.setActions(actions);
    }
}
