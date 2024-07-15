package com.nelmin.my_log.content.service;

import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.model.User;
import com.nelmin.my_log.common.service.FillContentInfo;
import com.nelmin.my_log.content.dto.Actions;
import com.nelmin.my_log.content.dto.ArticleDto;
import com.nelmin.my_log.content.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService implements FillContentInfo<ArticleDto> {
    private final UserInfo userInfo;
    private final User.Repo userRepo;
    private final SubscriptionsService subscriptionsService;

    @Override
    public void fillContentInfo(ArticleDto article) {
        try {
            userRepo.getIdByNickName(article.getAuthorName()).ifPresent(it -> {
                var status = Article.Status.valueOf(article.getStatus().toUpperCase());
                var userId = it.getId();
                var actions = new Actions();
                var currentUserIsOwner = Objects.equals(userId, userInfo.getId());

                if (currentUserIsOwner) {

                    actions.setCanDelete(true);
                    actions.setCanEdit(true);
                    actions.setCanPublish(Objects.equals(Article.Status.DRAFT, status));
                    actions.setCanUnpublish(List.of(
                            Article.Status.PRIVATE_PUBLISHED,
                            Article.Status.PUBLISHED,
                            Article.Status.PENDING).contains(status));

                } else if (status == Article.Status.PUBLISHED) {

                    var subscribed = subscriptionsService.isSubscribed(userId);
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
